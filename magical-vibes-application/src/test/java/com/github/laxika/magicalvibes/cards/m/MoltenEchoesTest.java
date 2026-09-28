package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KrenkosCommand;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MoltenEchoes.class, GrizzlyBears.class, HillGiant.class, KrenkosCommand.class})
class MoltenEchoesTest extends BaseCardTest {

    @Test
    void copiesMatchingNontokenCreatureWithHaste() {
        castEchoes(CardSubtype.BEAR);

        castCreature(new GrizzlyBears(), ManaColor.GREEN, 1, 1);

        List<Permanent> copies = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    void ignoresOtherTypesAndTokenCreatures() {
        castEchoes(CardSubtype.GOBLIN);

        harness.setHand(player1, List.of(new KrenkosCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);

        castCreature(new HillGiant(), ManaColor.RED, 2, 3);
        assertThat(findPermanents(player1, "Hill Giant"))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void copiesExileAtTheBeginningOfNextEndStep() {
        castEchoes(CardSubtype.BEAR);
        castCreature(new GrizzlyBears(), ManaColor.GREEN, 1, 1);

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(p -> p.getCard().isToken()).isEmpty();
    }

    private void castEchoes(CardSubtype chosenSubtype) {
        harness.setHand(player1, List.of(new MoltenEchoes()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, chosenSubtype.name());
    }

    private void castCreature(Card creature, ManaColor coloredMana, int coloredAmount, int colorlessAmount) {
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, coloredMana, coloredAmount);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessAmount);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
