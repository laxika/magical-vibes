package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.q.QuinjetTechnician;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VibraniumMiningMech.class, QuinjetTechnician.class})
class VibraniumMiningMechTest extends BaseCardTest {

    @Test
    void entersWithTappedIndestructibleVibraniumToken() {
        harness.setHand(player1, List.of(new VibraniumMiningMech()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent vibranium = findPermanent(player1, "Vibranium");
        assertThat(vibranium.isTapped()).isTrue();
        assertThat(vibranium.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.hasKeyword(gd, vibranium, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void attackingCreatesAnotherVibraniumToken() {
        Permanent mech = harness.addToBattlefieldAndReturn(player1, new VibraniumMiningMech());
        mech.setSummoningSick(false);
        Permanent crew = addCreatureReady(player1, new QuinjetTechnician());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mech), 1, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(mech)));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vibranium")).hasSize(1);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void activatedAbilityBoostsMechUntilEndOfTurn() {
        Permanent mech = harness.addToBattlefieldAndReturn(player1, new VibraniumMiningMech());
        mech.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mech), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mech)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mech)).isEqualTo(6);
    }

    @Test
    void vibraniumManaAbilityProducesPowerstoneMana() {
        harness.setHand(player1, List.of(new VibraniumMiningMech()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        Permanent token = findPermanent(player1, "Vibranium");
        token.untap();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token),
                0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }

    @Test
    void createdTokenHasVibraniumArtifactSubtype() {
        Permanent token = createVibraniumToken();

        assertThat(token.getCard().getSubtypes())
                .extracting(subtype -> subtype.getDisplayName())
                .contains("Vibranium");
    }

    @Test
    void pumpBeforeCrewingPersistsAndMultipleActivationsAccumulate() {
        Permanent mech = harness.addToBattlefieldAndReturn(player1, new VibraniumMiningMech());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new QuinjetTechnician());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, mech)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mech)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, mech)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mech)).isFalse();
        assertThat(gqs.getEffectivePower(gd, mech)).isEqualTo(6);
    }

    @Test
    void cannotCrewWithoutEnoughUntappedCreaturePower() {
        Permanent mech = harness.addToBattlefieldAndReturn(player1, new VibraniumMiningMech());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new QuinjetTechnician());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, mech)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void vibraniumManaCanPayForArtifactSpell() {
        Permanent token = createVibraniumToken();
        token.untap();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.setHand(player1, List.of(new VibraniumMiningMech()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vibranium Mining Mech")).hasSize(2);
        assertThat(findPermanents(player1, "Vibranium")).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    @Test
    void vibraniumManaCannotPayGenericPartOfNonartifactSpell() {
        Permanent token = createVibraniumToken();
        token.untap();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.setHand(player1, List.of(new QuinjetTechnician()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Quinjet Technician");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }

    @Test
    void vibraniumManaCanPayForPumpAbility() {
        Permanent token = createVibraniumToken();
        token.untap();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent mech = findPermanent(player1, "Vibranium Mining Mech");
        assertThat(gqs.getEffectivePower(gd, mech)).isEqualTo(7);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
        assertThat(findPermanents(player1, "Vibranium")).hasSize(1);
    }

    private Permanent createVibraniumToken() {
        harness.setHand(player1, List.of(new VibraniumMiningMech()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Vibranium");
    }
}
