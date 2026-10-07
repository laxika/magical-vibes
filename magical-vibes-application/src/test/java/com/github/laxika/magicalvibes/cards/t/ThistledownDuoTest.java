package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThistledownDuo.class, EliteVanguard.class, FugitiveWizard.class, GrizzlyBears.class})
class ThistledownDuoTest extends BaseCardTest {

    @BeforeEach
    void setUp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new ThistledownDuo());
    }

    private void castWhiteSpell() {
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
    }

    private void castBlueSpell() {
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
    }

    private Permanent duo() {
        return findPermanent(player1, "Thistledown Duo");
    }

    @Test
    @DisplayName("Casting a white spell gives Thistledown Duo +1/+1 until end of turn")
    void whiteSpellBoosts() {
        castWhiteSpell();
        harness.passBothPriorities(); // resolve the trigger

        assertThat(duo().getPowerModifier()).isEqualTo(1);
        assertThat(duo().getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a blue spell gives Thistledown Duo flying until end of turn")
    void blueSpellGrantsFlying() {
        assertThat(duo().hasKeyword(Keyword.FLYING)).isFalse();

        castBlueSpell();
        harness.passBothPriorities(); // resolve the trigger

        assertThat(duo().hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting a non-white, non-blue spell does not trigger either ability")
    void otherColorDoesNotTrigger() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(duo().getPowerModifier()).isEqualTo(0);
        assertThat(duo().hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOff() {
        castWhiteSpell();
        harness.passBothPriorities();
        assertThat(duo().getPowerModifier()).isEqualTo(1);

        resolveAllTriggers();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(duo().getPowerModifier()).isEqualTo(0);
        assertThat(duo().getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOff() {
        castBlueSpell();
        harness.passBothPriorities();
        assertThat(duo().hasKeyword(Keyword.FLYING)).isTrue();

        resolveAllTriggers();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(duo().hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A white-blue hybrid spell paid with white mana triggers both abilities")
    void hybridSpellPaidWithWhiteTriggersBothAbilities() {
        assertHybridSpellTriggersBothAbilities(ManaColor.WHITE);
    }

    @Test
    @DisplayName("A white-blue hybrid spell paid with blue mana triggers both abilities")
    void hybridSpellPaidWithBlueTriggersBothAbilities() {
        assertHybridSpellTriggersBothAbilities(ManaColor.BLUE);
    }

    private void assertHybridSpellTriggersBothAbilities(ManaColor paymentColor) {
        Permanent original = duo();
        harness.setHand(player1, List.of(new ThistledownDuo()));
        harness.addMana(player1, paymentColor, 3);
        harness.castCreature(player1, 0);

        assertThat(original.getPowerModifier()).isZero();
        assertThat(original.hasKeyword(Keyword.FLYING)).isFalse();
        resolveAllTriggers();

        assertThat(original.getPowerModifier()).isEqualTo(1);
        assertThat(original.getToughnessModifier()).isEqualTo(1);
        assertThat(original.hasKeyword(Keyword.FLYING)).isTrue();
        Permanent newlyCast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(newlyCast.getPowerModifier()).isZero();
        assertThat(newlyCast.hasKeyword(Keyword.FLYING)).isFalse();

        harness.passUntil(TurnStep.CLEANUP);
        assertThat(original.getPowerModifier()).isZero();
        assertThat(original.getToughnessModifier()).isZero();
        assertThat(original.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Repeated white spells give cumulative boosts")
    void repeatedWhiteSpellsStackBoosts() {
        castWhiteSpell();
        resolveAllTriggers();
        castWhiteSpell();
        resolveAllTriggers();

        assertThat(duo().getPowerModifier()).isEqualTo(2);
        assertThat(duo().getToughnessModifier()).isEqualTo(2);
        assertThat(duo().hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's white-blue spell triggers neither ability")
    void opponentsHybridSpellDoesNotTrigger() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ThistledownDuo()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(duo().getPowerModifier()).isZero();
        assertThat(duo().getToughnessModifier()).isZero();
        assertThat(duo().hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A white-blue creature entering without being cast triggers neither ability")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThistledownDuo());

        assertThat(gd.stack).isEmpty();
        assertThat(duo().getPowerModifier()).isZero();
        assertThat(duo().hasKeyword(Keyword.FLYING)).isFalse();
    }
}
