package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HydraBroodmaster;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.q.QueenAllenalOfRuadach;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({PolukranosReborn.class, PolukranosEngineOfRuin.class, HydraBroodmaster.class, Murder.class, QueenAllenalOfRuadach.class, WitnessProtection.class})
class PolukranosRebornTest extends BaseCardTest {

    @Test
    void transformsAtSorcerySpeedUsingPhyrexianMana() {
        Permanent polukranos = addPolukranos();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(polukranos.isTransformed()).isTrue();
        assertThat(polukranos.getCard()).isInstanceOf(PolukranosEngineOfRuin.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void createsReachAndLifelinkHydrasWhenItDies() {
        Permanent polukranos = addPolukranos();
        transform(polukranos);

        destroy(polukranos);

        List<Permanent> tokens = hydraTokens();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).anyMatch(token -> token.getCard().getKeywords().contains(Keyword.REACH));
        assertThat(tokens).anyMatch(token -> token.getCard().getKeywords().contains(Keyword.LIFELINK));
    }

    @Test
    void createsTokensWhenAnotherNontokenHydraYouControlDies() {
        Permanent polukranos = addPolukranos();
        transform(polukranos);
        Permanent otherHydra = harness.addToBattlefieldAndReturn(player1, new HydraBroodmaster());

        destroy(otherHydra);

        assertThat(hydraTokens()).hasSize(2);
    }

    @Test
    void transformsUsingWhiteManaWithoutPayingLife() {
        Permanent polukranos = addPolukranos();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(polukranos.isTransformed()).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotTransformDuringOpponentsMainPhase() {
        Permanent polukranos = addPolukranos();
        prepareMainPhase();
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(polukranos.isTransformed()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotTransformDuringUpkeep() {
        Permanent polukranos = addPolukranos();
        prepareMainPhase();
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(polukranos.isTransformed()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateTransformationWithNonemptyStack() {
        Permanent polukranos = addPolukranos();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 18);
        harness.passBothPriorities();
        assertThat(polukranos.isTransformed()).isTrue();
    }

    @Test
    void frontFaceDeathDoesNotCreateTokens() {
        Permanent polukranos = addPolukranos();

        destroy(polukranos);

        assertThat(hydraTokens()).isEmpty();
        harness.assertNotOnBattlefield(player1, "Polukranos Reborn");
    }

    @Test
    void opponentsNontokenHydraDeathDoesNotCreateTokens() {
        Permanent polukranos = addPolukranos();
        transform(polukranos);
        Permanent opposingHydra = harness.addToBattlefieldAndReturn(player2, new PolukranosReborn());

        destroy(opposingHydra);

        assertThat(hydraTokens()).isEmpty();
        harness.assertNotOnBattlefield(player2, "Polukranos Reborn");
    }

    @Test
    void tokenHydraDeathDoesNotCreateMoreTokens() {
        Permanent polukranos = addPolukranos();
        transform(polukranos);
        Permanent otherHydra = harness.addToBattlefieldAndReturn(player1, new PolukranosReborn());
        destroy(otherHydra);
        assertThat(hydraTokens()).hasSize(2);

        destroy(hydraTokens().getFirst());

        assertThat(hydraTokens()).hasSize(1);
    }

    @Test
    void bothHydrasAreCreatedInOneEventForQueenAllenal() {
        Permanent polukranos = addPolukranos();
        transform(polukranos);
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());

        destroy(polukranos);

        assertThat(hydraTokens()).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .filteredOn(permanent -> permanent.getCard().getName().equals("Soldier"))
                .hasSize(1);
    }

    @Test
    void creatureThatLostHydraSubtypeDoesNotTriggerTokens() {
        Permanent polukranos = addPolukranos();
        transform(polukranos);
        Permanent otherHydra = harness.addToBattlefieldAndReturn(player1, new PolukranosReborn());
        prepareMainPhase();
        harness.setHand(player1, List.of(new WitnessProtection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, otherHydra.getId());
        harness.passBothPriorities();

        destroy(otherHydra);

        assertThat(hydraTokens()).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(polukranos).doesNotContain(otherHydra);
    }

    @Test
    void simultaneousDeathWithAnotherHydraTriggersForBothCreatures() {
        Permanent polukranos = addPolukranos();
        transform(polukranos);
        Permanent otherHydra = harness.addToBattlefieldAndReturn(player1, new PolukranosReborn());
        prepareMainPhase();
        polukranos.setMarkedDamage(6);
        otherHydra.setMarkedDamage(5);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hydraTokens()).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(polukranos, otherHydra);
    }

    @Test
    void createsTwoDistinctTokenProfilesWithOracleCharacteristics() {
        Permanent polukranos = addPolukranos();
        transform(polukranos);

        destroy(polukranos);

        List<Permanent> tokens = hydraTokens();
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(3);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.HYDRA);
        }
        assertThat(tokens).filteredOn(token -> token.getCard().getKeywords().contains(Keyword.REACH))
                .singleElement().satisfies(token -> assertThat(token.getCard().getKeywords()).doesNotContain(Keyword.LIFELINK));
        assertThat(tokens).filteredOn(token -> token.getCard().getKeywords().contains(Keyword.LIFELINK))
                .singleElement().satisfies(token -> assertThat(token.getCard().getKeywords()).doesNotContain(Keyword.REACH));
    }

    private Permanent addPolukranos() {
        return harness.addToBattlefieldAndReturn(player1, new PolukranosReborn());
    }

    private void transform(Permanent polukranos) {
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(polukranos);
        harness.activateAbility(player1, permanentIndex, null, null);
        harness.passBothPriorities();
    }

    private void destroy(Permanent permanent) {
        prepareMainPhase();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, permanent.getId());
        harness.passBothPriorities();
    }

    private List<Permanent> hydraTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Phyrexian Hydra"))
                .toList();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
