package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpellmorphRaiseDead.class, com.github.laxika.magicalvibes.cards.g.GrizzlyBears.class, com.github.laxika.magicalvibes.cards.h.Humility.class, com.github.laxika.magicalvibes.cards.r.RuleOfLaw.class})
class SpellmorphRaiseDeadTest extends BaseCardTest {

    @Test
    void returnsCreatureFromGraveyardWhenCastWithSpellmorph() {
        Card bears = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        Permanent spellmorph = castFaceDown();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.ensurePriority(player1);
        gs.playCardWithSpellmorph(gd, player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spellmorph), 0, bears.getId(), List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spellmorph);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.INSTANT_SPELL
                && entry.getCard().getName().equals("Spellmorph Raise Dead")
                && entry.getSourceZone() == com.github.laxika.magicalvibes.model.Zone.BATTLEFIELD);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spellmorph.getCard());
    }

    @Test
    void cannotTurnSpellmorphFaceUp() {
        Permanent spellmorph = castFaceDown();

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(spellmorph)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cast from the battlefield");
        assertThat(spellmorph.isFaceDown()).isTrue();
    }


    @Test
    void returnsCreatureWhenCastNormallyFromHand() {
        Card bears = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        SpellmorphRaiseDead spell = new SpellmorphRaiseDead();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell).doesNotContain(bears);
    }

    @Test
    void cannotCastWithSpellmorphWithoutMana() {
        Card bears = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        Permanent spellmorph = castFaceDown();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardWithSpellmorph(gd, player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spellmorph), 0, bears.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spellmorph);
        assertThat(spellmorph.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetOpponentsCreatureWithSpellmorph() {
        Card bears = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        Permanent spellmorph = castFaceDown();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardWithSpellmorph(gd, player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spellmorph), 0, bears.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spellmorph);
        assertThat(spellmorph.isFaceDown()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNoncreatureWithSpellmorph() {
        Card noncreature = new SpellmorphRaiseDead();
        harness.setGraveyard(player1, List.of(noncreature));
        Permanent spellmorph = castFaceDown();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardWithSpellmorph(gd, player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spellmorph), 0, noncreature.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spellmorph);
        assertThat(spellmorph.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReturnTargetThatLeavesGraveyardBeforeResolution() {
        Card bears = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        Permanent spellmorph = castFaceDown();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.ensurePriority(player1);
        gs.playCardWithSpellmorph(gd, player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spellmorph), 0, bears.getId(), List.of());
        harness.setGraveyard(player1, List.of());
        gd.addToExile(player1.getId(), bears);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spellmorph.getCard());
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    void cannotCastWithSpellmorphWhileHumilityRemovesAbilities() {
        Card bears = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        Permanent spellmorph = castFaceDown();
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.h.Humility());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardWithSpellmorph(gd, player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spellmorph), 0, bears.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spellmorph);
        assertThat(spellmorph.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }


    @Test
    void cannotCastWithSpellmorphWithoutTarget() {
        Permanent spellmorph = castFaceDown();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardWithSpellmorph(gd, player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spellmorph), 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spellmorph);
        assertThat(spellmorph.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void spellmorphCannotBypassRuleOfLaw() {
        Card bears = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.r.RuleOfLaw());
        Permanent spellmorph = castFaceDown();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardWithSpellmorph(gd, player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spellmorph), 0, bears.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spellmorph);
        assertThat(spellmorph.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castFaceDown() {
        SpellmorphRaiseDead card = new SpellmorphRaiseDead();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Spellmorph Raise Dead");
    }
}
