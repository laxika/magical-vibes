package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EverquillPhoenix.class, AlmightyBrushwagg.class})
class EverquillPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating creates a red Feather artifact token")
    void mutatingCreatesFeatherToken() {
        Permanent phoenix = addCreatureReady(player1, new EverquillPhoenix());

        triggerMutation(phoenix);

        Permanent feather = findPermanent(player1, "Feather");
        assertThat(feather.getCard().isToken()).isTrue();
        assertThat(feather.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(feather.getCard().getColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Sacrificing a Feather returns a target Phoenix tapped")
    void featherReturnsTargetPhoenixTapped() {
        Permanent phoenix = addCreatureReady(player1, new EverquillPhoenix());
        EverquillPhoenix phoenixCard = new EverquillPhoenix();
        harness.setGraveyard(player1, List.of(phoenixCard));
        triggerMutation(phoenix);
        Permanent feather = findPermanent(player1, "Feather");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(feather),
                null, phoenixCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Feather")).isEmpty();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(phoenixCard.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Everquill Phoenix");
    }

    @Test
    @DisplayName("Feather cannot target a non-Phoenix card")
    void featherCannotTargetNonPhoenixCard() {
        Permanent phoenix = addCreatureReady(player1, new EverquillPhoenix());
        AlmightyBrushwagg brushwagg = new AlmightyBrushwagg();
        harness.setGraveyard(player1, List.of(brushwagg));
        triggerMutation(phoenix);
        Permanent feather = findPermanent(player1, "Feather");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(feather),
                null, brushwagg.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting for the mutate cost creates exactly one Feather")
    void mutateSpellCreatesFeather() {
        Permanent host = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new EverquillPhoenix()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, host.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Feather")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player2, "Feather")).isEmpty();
    }

    @Test
    @DisplayName("Casting normally does not create a Feather")
    void normalCastDoesNotCreateFeather() {
        harness.setHand(player1, List.of(new EverquillPhoenix()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Everquill Phoenix");
        assertThat(findPermanents(player1, "Feather")).isEmpty();
    }

    @Test
    @DisplayName("Each mutation creates one additional Feather")
    void repeatedMutationsCreateOneFeatherEach() {
        Permanent phoenix = addCreatureReady(player1, new EverquillPhoenix());

        triggerMutation(phoenix);
        triggerMutation(phoenix);

        assertThat(countPermanents(player1, "Feather")).isEqualTo(2);
    }

    @Test
    @DisplayName("Feather cannot target a Phoenix in an opponent's graveyard")
    void featherCannotTargetOpponentsPhoenix() {
        Permanent phoenix = addCreatureReady(player1, new EverquillPhoenix());
        EverquillPhoenix target = new EverquillPhoenix();
        harness.setGraveyard(player2, List.of(target));
        triggerMutation(phoenix);
        Permanent feather = findPermanent(player1, "Feather");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(feather),
                null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player2, "Everquill Phoenix");
        harness.assertOnBattlefield(player1, "Feather");
    }

    @Test
    @DisplayName("Feather requires one mana in addition to being sacrificed")
    void featherCannotBeActivatedWithoutMana() {
        Permanent phoenix = addCreatureReady(player1, new EverquillPhoenix());
        EverquillPhoenix target = new EverquillPhoenix();
        harness.setGraveyard(player1, List.of(target));
        triggerMutation(phoenix);
        Permanent feather = findPermanent(player1, "Feather");

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(feather),
                null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Feather");
        harness.assertInGraveyard(player1, "Everquill Phoenix");
    }

    @Test
    @DisplayName("Feather is sacrificed as a cost even if its target leaves the graveyard")
    void featherStaysSacrificedWhenTargetLeavesGraveyard() {
        Permanent phoenix = addCreatureReady(player1, new EverquillPhoenix());
        EverquillPhoenix target = new EverquillPhoenix();
        harness.setGraveyard(player1, List.of(target));
        triggerMutation(phoenix);
        Permanent feather = findPermanent(player1, "Feather");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(feather),
                null, target.getId(), Zone.GRAVEYARD);
        assertThat(findPermanents(player1, "Feather")).isEmpty();
        harness.assertInGraveyard(player1, "Everquill Phoenix");
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Everquill Phoenix")).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        harness.assertNotInGraveyard(player1, "Everquill Phoenix");
        assertThat(findPermanents(player1, "Feather")).isEmpty();
    }

    private void triggerMutation(Permanent phoenix) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, phoenix, List.of(phoenix.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
