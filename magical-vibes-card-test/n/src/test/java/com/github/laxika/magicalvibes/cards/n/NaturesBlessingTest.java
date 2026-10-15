package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({NaturesBlessing.class, StormCrow.class})
class NaturesBlessingTest extends BaseCardTest {

    private static final String COUNTER_MODE = "Put a +1/+1 counter on it";
    private static final String TRAMPLE_MODE = "It gains trample";
    private static final String BANDING_MODE = "It gains banding";
    private static final String FIRST_STRIKE_MODE = "It gains first strike";

    private Permanent setUpBlessing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new NaturesBlessing());
        return harness.addToBattlefieldAndReturn(player1, new StormCrow());
    }

    private void activate(Player player, Permanent target) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.setHand(player, List.of(new StormCrow()));

        harness.activateAbility(player, 0, null, target.getId());
        harness.handleCardChosen(player, 0); // pay the discard cost
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on the target creature")
    void counterMode() {
        Permanent creature = setUpBlessing();
        int power = gqs.getEffectivePower(gd, creature);
        int toughness = gqs.getEffectiveToughness(gd, creature);

        activate(player1, creature);
        harness.handleListChoice(player1, COUNTER_MODE);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughness + 1);
        harness.assertInGraveyard(player1, "Storm Crow");
    }

    @Test
    @DisplayName("Trample mode grants trample and it lasts past end of turn")
    void trampleModeLastsIndefinitely() {
        Permanent creature = setUpBlessing();

        activate(player1, creature);
        harness.handleListChoice(player1, TRAMPLE_MODE);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Banding mode grants banding, not the other modes' keywords")
    void bandingMode() {
        Permanent creature = setUpBlessing();

        activate(player1, creature);
        harness.handleListChoice(player1, BANDING_MODE);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First-strike mode grants first strike to the target creature")
    void firstStrikeMode() {
        Permanent creature = setUpBlessing();

        activate(player1, creature);
        harness.handleListChoice(player1, FIRST_STRIKE_MODE);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ability can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new NaturesBlessing());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StormCrow());

        activate(player1, creature);
        harness.handleListChoice(player1, TRAMPLE_MODE);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The ability can only target a creature")
    void cannotTargetNoncreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent blessing = harness.addToBattlefieldAndReturn(player1, new NaturesBlessing());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new StormCrow()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blessing.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Repeated activations accumulate counters and different keywords")
    void repeatedActivationsAccumulate() {
        Permanent creature = setUpBlessing();
        int power = gqs.getEffectivePower(gd, creature);
        int toughness = gqs.getEffectiveToughness(gd, creature);

        activate(player1, creature);
        harness.handleListChoice(player1, COUNTER_MODE);
        activate(player1, creature);
        harness.handleListChoice(player1, COUNTER_MODE);
        activate(player1, creature);
        harness.handleListChoice(player1, BANDING_MODE);
        activate(player1, creature);
        harness.handleListChoice(player1, FIRST_STRIKE_MODE);
        activate(player1, creature);
        harness.handleListChoice(player1, TRAMPLE_MODE);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(power + 2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughness + 2);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An activated ability resolves after the enchantment leaves the battlefield")
    void abilitySurvivesSourceLeaving() {
        Permanent creature = setUpBlessing();
        Permanent blessing = findPermanent(player1, "Nature's Blessing");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new NaturesBlessing()));

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Nature's Blessing");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, blessing));
        harness.passBothPriorities();
        harness.handleListChoice(player1, FIRST_STRIKE_MODE);

        harness.assertNotOnBattlefield(player1, "Nature's Blessing");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void cannotActivateWithEmptyHand() {
        Permanent creature = setUpBlessing();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
