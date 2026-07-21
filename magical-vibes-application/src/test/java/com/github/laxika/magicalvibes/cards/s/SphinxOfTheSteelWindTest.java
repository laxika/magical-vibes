package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SphinxOfTheSteelWindTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createFlyer(String name, int power, int toughness, CardColor color) {
        Card card = createCreature(name, power, toughness, color);
        card.setKeywords(Set.of(Keyword.FLYING));
        return card;
    }

    private static Card createTargetedInstant(String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    // ===== Protection - blocking =====

    @Test
    @DisplayName("Red creature cannot block Sphinx of the Steel Wind")
    void redCreatureCannotBlock() {
        Permanent attacker = new Permanent(new SphinxOfTheSteelWind());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createFlyer("Fire Elemental", 5, 5, CardColor.RED));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature cannot block Sphinx of the Steel Wind")
    void greenCreatureCannotBlock() {
        Permanent attacker = new Permanent(new SphinxOfTheSteelWind());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createFlyer("Craw Wurm", 6, 4, CardColor.GREEN));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Blue creature can block Sphinx of the Steel Wind")
    void blueCreatureCanBlock() {
        Permanent attacker = new Permanent(new SphinxOfTheSteelWind());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createFlyer("Wind Drake", 2, 2, CardColor.BLUE));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    // ===== Protection - combat damage =====

    @Test
    @DisplayName("Sphinx takes no combat damage from red creature")
    void takesNoDamageFromRed() {
        Permanent attacker = new Permanent(createCreature("Fire Elemental", 7, 7, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new SphinxOfTheSteelWind());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Sphinx deals 6 first strike (7/7 survives); red's 7 damage is prevented (protection)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sphinx of the Steel Wind"));
    }

    @Test
    @DisplayName("Sphinx takes no combat damage from green creature")
    void takesNoDamageFromGreen() {
        Permanent attacker = new Permanent(createCreature("Craw Wurm", 7, 7, CardColor.GREEN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new SphinxOfTheSteelWind());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Sphinx deals 6 first strike (7/7 survives); green's 7 damage is prevented (protection)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sphinx of the Steel Wind"));
    }

    @Test
    @DisplayName("Sphinx takes normal combat damage from blue creature")
    void takesNormalDamageFromBlue() {
        Permanent attacker = new Permanent(createCreature("Blue Ogre", 7, 7, CardColor.BLUE));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new SphinxOfTheSteelWind());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Sphinx deals 6 first strike (7/7 survives); blue's 7 damage kills the 6/6 Sphinx (no protection)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sphinx of the Steel Wind"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Sphinx of the Steel Wind"));
    }

    // ===== Protection - targeting =====

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRed() {
        Permanent sphinx = new Permanent(new SphinxOfTheSteelWind());
        sphinx.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(sphinx);

        // Add valid target so spell is playable
        Permanent decoy = new Permanent(createCreature("Wind Drake", 2, 2, CardColor.BLUE));
        decoy.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(decoy);

        harness.setHand(player1, List.of(createTargetedInstant("Lightning Bolt", CardColor.RED, "{R}")));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, sphinx.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Cannot be targeted by green instant")
    void cannotBeTargetedByGreen() {
        Permanent sphinx = new Permanent(new SphinxOfTheSteelWind());
        sphinx.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(sphinx);

        // Add valid target so spell is playable
        Permanent decoy = new Permanent(createCreature("Wind Drake", 2, 2, CardColor.BLUE));
        decoy.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(decoy);

        harness.setHand(player1, List.of(createTargetedInstant("Prey Upon", CardColor.GREEN, "{G}")));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, sphinx.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Can be targeted by blue instant")
    void canBeTargetedByBlue() {
        Permanent sphinx = new Permanent(new SphinxOfTheSteelWind());
        sphinx.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sphinx);

        harness.setHand(player1, List.of(createTargetedInstant("Slice", CardColor.BLUE, "{U}")));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, sphinx.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Slice");
    }
}
