package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HavenwoodWurm;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavageThallid.class, HavenwoodWurm.class, SuddenShock.class})
class SavageThallidTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent thallid = addThallid();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during an opponent's upkeep")
    void upkeepTriggerOnlyFiresDuringControllerUpkeep() {
        Permanent thallid = addThallid();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling token")
    void removesThreeSporeCountersAndCreatesToken() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(findPermanents(player1, "Saproling"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColors()).containsExactly(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                });
    }

    @Test
    @DisplayName("Removing three spore counters leaves additional counters")
    void removesExactlyThreeSporeCounters() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isOne();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Saproling regenerates the target Fungus")
    void sacrificingSaprolingRegeneratesTargetFungus() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent target = addCreatureReady(player2, new SavageThallid());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The token ability requires three spore counters")
    void tokenAbilityRequiresThreeSporeCounters() {
        addThallid().setCounterCount(CounterType.FUNGUS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The regeneration ability cannot target a non-Fungus")
    void regenerationAbilityRequiresFungusTarget() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent target = addCreatureReady(player2, new HavenwoodWurm());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Fungus");
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("The regeneration ability requires a Saproling to sacrifice")
    void regenerationAbilityRequiresSaprolingSacrifice() {
        addThallid();
        Permanent target = addCreatureReady(player2, new SavageThallid());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regeneration protects the source from lethal damage and consumes the shield")
    void regenerationProtectsSourceFromLethalDamage() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, thallid.getId());

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(thallid.getRegenerationShield()).isZero();
        harness.passBothPriorities();
        assertThat(thallid.isTapped()).isFalse();

        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, thallid.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(thallid);
        assertThat(thallid.isTapped()).isTrue();
        assertThat(thallid.getRegenerationShield()).isZero();
        assertThat(thallid.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player1, "Savage Thallid");
    }

    @Test
    @DisplayName("An opponent's Saproling cannot pay the regeneration cost")
    void cannotSacrificeOpponentsSaproling() {
        Permanent thallid = addThallid();
        Permanent opponentThallid = addCreatureReady(player2, new SavageThallid());
        opponentThallid.setCounterCount(CounterType.FUNGUS, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, thallid.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
        assertThat(thallid.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Thallid can create a Saproling")
    void tokenAbilityDoesNotRequireTappingOrHaste() {
        Permanent thallid = addThallid();
        thallid.setSummoningSick(true);
        thallid.tap();
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(thallid.isTapped()).isTrue();
    }

    private Permanent addThallid() {
        return addCreatureReady(player1, new SavageThallid());
    }
}
