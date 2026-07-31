package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RitualOfTheMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and permanently steals the target")
    void stealsTargetCreature() {
        Permanent fodder = addToPlayer1(new LlanowarElves());
        Permanent stolen = addToPlayer2(new GrizzlyBears());

        harness.setHand(player1, List.of(new RitualOfTheMachine()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, stolen.getId(), fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(stolen);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stolen);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent fodder = addToPlayer1(new LlanowarElves());
        Permanent imp = addToPlayer2(blackCreature());

        harness.setHand(player1, List.of(new RitualOfTheMachine()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, imp.getId(), fodder.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        Permanent fodder = addToPlayer1(new LlanowarElves());
        Permanent thopter = addToPlayer2(new Ornithopter());

        harness.setHand(player1, List.of(new RitualOfTheMachine()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, thopter.getId(), fodder.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast without a creature to sacrifice")
    void cannotCastWithoutSacrifice() {
        Permanent stolen = addToPlayer2(new GrizzlyBears());

        harness.setHand(player1, List.of(new RitualOfTheMachine()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, stolen.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addToPlayer1(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private Permanent addToPlayer2(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(permanent);
        return permanent;
    }

    private Card blackCreature() {
        Card card = new Card();
        card.setName("Bog Imp");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{B}");
        card.setColor(CardColor.BLACK);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
