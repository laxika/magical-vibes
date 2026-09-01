package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ClockworkGnomes;
import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrokenVisage.class, DwarvenTrader.class, ClockworkGnomes.class})
class BrokenVisageTest extends BaseCardTest {

    private void castBrokenVisage(UUID targetId) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrokenVisage()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetId);
    }

    private Permanent addAttacker(Player owner) {
        return addAttacker(owner, new DwarvenTrader());
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent findSpiritToken(Player controller) {
        return findPermanent(controller, "Spirit");
    }

    @Test
    @DisplayName("Destroys the attacking creature and creates a Spirit token with its power/toughness")
    void destroysAndCreatesSpirit() {
        Permanent attacker = addAttacker(player1);

        castBrokenVisage(attacker.getId());
        harness.passBothPriorities();

        // Dwarven Trader (1/1) destroyed -> owner's graveyard.
        harness.assertNotOnBattlefield(player1, "Dwarven Trader");
        harness.assertInGraveyard(player1, "Dwarven Trader");

        // Caster gets a 1/1 Spirit token.
        Permanent spirit = findSpiritToken(player2);
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Spirit token's power/toughness reflect the destroyed creature's modified stats")
    void spiritCopiesModifiedStats() {
        Permanent attacker = addAttacker(player1);
        attacker.setPowerModifier(3);   // 1 + 3 = 4
        attacker.setToughnessModifier(1); // 1 + 1 = 2

        castBrokenVisage(attacker.getId());
        harness.passBothPriorities();

        Permanent spirit = findSpiritToken(player2);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Spirit token is sacrificed at the beginning of the next end step")
    void sacrificesSpiritAtEndStep() {
        Permanent attacker = addAttacker(player1);

        castBrokenVisage(attacker.getId());
        harness.passBothPriorities();

        // Token exists after resolution.
        harness.assertOnBattlefield(player2, "Spirit");

        // Advance to the end step — the token should be sacrificed.
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addAttacker(player2); // legal target elsewhere so the spell is playable
        harness.addToBattlefield(player1, new DwarvenTrader());
        UUID targetId = harness.getPermanentId(player1, "Dwarven Trader");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrokenVisage()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        addAttacker(player2); // legal nonartifact target elsewhere so the spell is playable
        Permanent artifactAttacker = addAttacker(player1, new ClockworkGnomes());

        assertThatThrownBy(() -> castBrokenVisage(artifactAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates the Spirit token even when the target is indestructible")
    void createsSpiritWhenTargetIsIndestructible() {
        Card indestructibleCard = new DwarvenTrader();
        indestructibleCard.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        Permanent attacker = addAttacker(player1, indestructibleCard);

        castBrokenVisage(attacker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dwarven Trader");
        Permanent spirit = findSpiritToken(player2);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be regenerated prevents a regeneration shield from replacing destruction")
    void destructionIgnoresRegenerationShield() {
        Permanent attacker = addAttacker(player1);
        attacker.setRegenerationShield(1);

        castBrokenVisage(attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dwarven Trader");
        harness.assertInGraveyard(player1, "Dwarven Trader");
        harness.assertOnBattlefield(player2, "Spirit");
    }

    @Test
    @DisplayName("Fizzles when the target stops attacking before resolution")
    void fizzlesWhenTargetStopsAttackingBeforeResolution() {
        Permanent attacker = addAttacker(player1);

        castBrokenVisage(attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dwarven Trader");
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }
}
