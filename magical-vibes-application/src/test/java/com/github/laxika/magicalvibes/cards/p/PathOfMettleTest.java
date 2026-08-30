package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PathOfMettleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage only to creatures without speed keywords")
    void etbDamagesOnlySlowCreatures() {
        Permanent slowControllerCreature = addCreature(player1, "Slow controller creature");
        Permanent fastControllerCreature = addCreature(player1, "Fast controller creature", Keyword.HASTE);
        Permanent slowOpponentCreature = addCreature(player2, "Slow opponent creature");
        Permanent fastOpponentCreature = addCreature(player2, "Fast opponent creature", Keyword.VIGILANCE);

        castPathOfMettle();

        assertThat(slowControllerCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(fastControllerCreature.getMarkedDamage()).isZero();
        assertThat(slowOpponentCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(fastOpponentCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Transforms after attacking with two qualifying creatures")
    void transformsWithTwoQualifyingAttackers() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new PathOfMettle());
        Permanent firstAttacker = addCreature(player1, "First attacker", Keyword.HASTE);
        Permanent secondAttacker = addCreature(player1, "Second attacker", Keyword.VIGILANCE);

        declareAttackers(player1, firstAttacker, secondAttacker);
        harness.passBothPriorities();

        assertThat(path.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform when fewer than two attackers have speed keywords")
    void doesNotTransformWithOnlyOneQualifyingAttacker() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new PathOfMettle());
        Permanent fastAttacker = addCreature(player1, "Fast attacker", Keyword.HASTE);
        Permanent slowAttacker = addCreature(player1, "Slow attacker");

        declareAttackers(player1, fastAttacker, slowAttacker);
        harness.passBothPriorities();

        assertThat(path.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Metzali adds one mana of a chosen color")
    void metzaliAddsAnyColorMana() {
        Permanent metzali = addTransformedMetzali(player1);

        harness.activateAbility(player1, indexOf(player1, metzali), 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Metzali deals 2 damage to each opponent")
    void metzaliDamagesOpponent() {
        Permanent metzali = addTransformedMetzali(player1);
        int lifeBefore = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, indexOf(player1, metzali), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Metzali destroys a creature that attacked this turn")
    void metzaliDestroysAttackedCreature() {
        Permanent metzali = addTransformedMetzali(player1);
        Permanent attacker = addCreature(player2, "Attacked creature");
        attacker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, metzali), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId).contains(attacker.getCard().getId());
    }

    private void castPathOfMettle() {
        harness.setHand(player1, List.of(new PathOfMettle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void declareAttackers(Player player, Permanent... attackers) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        List<Integer> indices = java.util.Arrays.stream(attackers)
                .map(attacker -> indexOf(player, attacker))
                .toList();
        gs.declareAttackers(gd, player, indices);
    }

    private Permanent addTransformedMetzali(Player player) {
        PathOfMettle card = new PathOfMettle();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreature(Player player, String name, Keyword... keywords) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        EnumSet<Keyword> keywordSet = EnumSet.noneOf(Keyword.class);
        keywordSet.addAll(List.of(keywords));
        card.setKeywords(keywordSet);

        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
