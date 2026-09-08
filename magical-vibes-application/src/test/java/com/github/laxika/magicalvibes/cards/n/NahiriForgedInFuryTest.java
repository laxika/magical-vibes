package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NahiriForgedInFury.class, Bonesplitter.class, GrizzlyBears.class})
class NahiriForgedInFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Equipment reduces Nahiri's generic casting cost")
    void affinityForEquipmentReducesGenericCastingCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Bonesplitter());
        }
        harness.setHand(player1, List.of(new NahiriForgedInFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only Equipment controlled by Nahiri's controller")
    void affinityCountsOnlyControlledEquipment() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Bonesplitter());
        }
        harness.setHand(player1, List.of(new NahiriForgedInFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("An attack exiles the top card and lets the controller cast an Equipment for free")
    void attackExilesTopCardAndCastsEquipmentForFree() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        Permanent nahiri = addReady(player1, new NahiriForgedInFury());
        Permanent equipment = addReady(player1, new Bonesplitter());
        equipment.setAttachedTo(attacker.getId());
        Card topCard = new Bonesplitter();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(nahiri).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("An exiled non-Equipment card keeps its normal casting cost")
    void attackExilesNonEquipmentWithNormalCastingCost() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        Permanent equipment = addReady(player1, new Bonesplitter());
        addReady(player1, new NahiriForgedInFury());
        equipment.setAttachedTo(attacker.getId());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("An Equipment controlled by Nahiri does not trigger for an opponent's equipped creature")
    void doesNotTriggerForOpponentEquippedCreature() {
        Permanent attacker = addReady(player2, new GrizzlyBears());
        addReady(player1, new NahiriForgedInFury());
        Permanent equipment = addReady(player1, new Bonesplitter());
        equipment.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new Bonesplitter()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Nahiri, Forged in Fury"));
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
