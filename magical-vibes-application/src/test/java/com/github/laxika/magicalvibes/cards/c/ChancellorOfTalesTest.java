package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartsDesire;
import com.github.laxika.magicalvibes.cards.l.LovestruckBeast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChancellorOfTales.class, GrizzlyBears.class, HeartsDesire.class, LovestruckBeast.class})
class ChancellorOfTalesTest extends BaseCardTest {

    @Test
    void acceptingTheOptionalCopyCopiesAnAdventureSpell() {
        harness.addToBattlefield(player1, new ChancellorOfTales());
        harness.setHand(player1, List.of(new LovestruckBeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())
                .filter(Card::isToken)
                .count();
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    void decliningDoesNotCopyTheAdventureSpell() {
        harness.addToBattlefield(player1, new ChancellorOfTales());
        harness.setHand(player1, List.of(new LovestruckBeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())
                .filter(Card::isToken)
                .count();
        assertThat(tokenCount).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForNormalCreatureSpell() {
        harness.addToBattlefield(player1, new ChancellorOfTales());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }
}
