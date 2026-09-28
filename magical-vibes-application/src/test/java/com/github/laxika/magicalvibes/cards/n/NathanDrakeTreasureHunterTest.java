package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZofShade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NathanDrakeTreasureHunter.class, Divination.class, GrizzlyBears.class, ZofShade.class})
class NathanDrakeTreasureHunterTest extends BaseCardTest {

    @Test
    @DisplayName("attacking exiles the top card of each library and offers every exiled spell")
    void attackingExilesTopCardsAndOffersSpells() {
        addCreatureReady(player1, new NathanDrakeTreasureHunter());
        Card ownSpell = new Divination();
        Card opposingSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownSpell));
        harness.setLibrary(player2, List.of(opposingSpell));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownSpell);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opposingSpell);
        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownSpell.getId(), opposingSpell.getId());
    }

    @Test
    @DisplayName("lets its controller use any mana for spells they do not own")
    void castsNonOwnedSpellWithAnyMana() {
        addCreatureReady(player1, new NathanDrakeTreasureHunter());
        Card opposingSpell = new GrizzlyBears();
        opposingSpell.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(opposingSpell));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("lets its controller use any mana for abilities of non-owned permanents")
    void activatesNonOwnedPermanentAbilityWithAnyMana() {
        addCreatureReady(player1, new NathanDrakeTreasureHunter());
        ZofShade opposingPermanent = new ZofShade();
        opposingPermanent.setOwnerId(player2.getId());
        Permanent zofShade = harness.addToBattlefieldAndReturn(player1, opposingPermanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gqs.getEffectivePower(gd, zofShade)).isEqualTo(4);
    }
}
