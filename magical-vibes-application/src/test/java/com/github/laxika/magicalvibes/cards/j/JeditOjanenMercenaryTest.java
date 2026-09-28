package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JeditOjanenMercenary.class, AdelizTheCinderWind.class, GrizzlyBears.class})
class JeditOjanenMercenaryTest extends BaseCardTest {

    @Test
    void ownEntryMayCreateCatWarriorToken() {
        harness.setHand(player1, List.of(new JeditOjanenMercenary()));
        addJeditMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertCatWarriorToken();
    }

    @Test
    void anotherLegendaryCreatureEntryMayCreateCatWarriorToken() {
        harness.addToBattlefield(player1, new JeditOjanenMercenary());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertCatWarriorToken();
    }

    @Test
    void nonlegendaryCreatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new JeditOjanenMercenary());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Cat Warrior")).isEmpty();
    }

    @Test
    void decliningPaymentDoesNotCreateCatWarriorToken() {
        harness.setHand(player1, List.of(new JeditOjanenMercenary()));
        addJeditMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Cat Warrior")).isEmpty();
    }

    private void addJeditMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void assertCatWarriorToken() {
        List<Permanent> tokens = findPermanents(player1, "Cat Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CAT, CardSubtype.WARRIOR);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FORESTWALK);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }
}
