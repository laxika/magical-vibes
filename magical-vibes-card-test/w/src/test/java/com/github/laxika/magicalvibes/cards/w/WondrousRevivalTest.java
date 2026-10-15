package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BlackBoltInhumanKing;
import com.github.laxika.magicalvibes.cards.b.BlackWidowAgileAvenger;
import com.github.laxika.magicalvibes.cards.i.IronManArmoredAvenger;
import com.github.laxika.magicalvibes.cards.z.ZuriWarriorOfWakanda;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WondrousRevival.class, BlackBoltInhumanKing.class, BlackWidowAgileAvenger.class,
        IronManArmoredAvenger.class, ZuriWarriorOfWakanda.class})
class WondrousRevivalTest extends BaseCardTest {

    @Test
    void returnsUpToThreeHeroCreaturesAndExcludesNonHeroes() {
        Card first = new BlackBoltInhumanKing();
        Card second = new BlackWidowAgileAvenger();
        Card third = new IronManArmoredAvenger();
        Card fourth = new ZuriWarriorOfWakanda();
        Card nonHero = new WondrousRevival();
        Card spell = new WondrousRevival();
        harness.setGraveyard(player1, List.of(first, second, third, fourth, nonHero));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                first.getId(), second.getId(), third.getId(), fourth.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(fourth.getId(), nonHero.getId(), spell.getId());
    }

    @Test
    void canChooseZeroTargetsEvenWithAnEligibleHero() {
        Card hero = new BlackWidowAgileAvenger();
        Card spell = new WondrousRevival();
        harness.setGraveyard(player1, List.of(hero));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(hero, spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canResolveWithNoEligibleCardsInOwnGraveyard() {
        Card opposingHero = new BlackWidowAgileAvenger();
        Card spell = new WondrousRevival();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opposingHero));

        harness.castFromHand(player1, spell, "{4}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingHero);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsRemainingLegalTargetWithoutReturningUnchosenHeroes() {
        Card first = new BlackBoltInhumanKing();
        Card second = new BlackWidowAgileAvenger();
        Card unchosen = new ZuriWarriorOfWakanda();
        Card opposingHero = new IronManArmoredAvenger();
        harness.setGraveyard(player1, List.of(first, second, unchosen));
        harness.setGraveyard(player2, List.of(opposingHero));
        harness.setHand(player1, List.of(new WondrousRevival()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, List.of());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                first.getId(), second.getId(), unchosen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player1, List.of(second, unchosen));
        harness.setHand(player1, List.of(first));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unchosen).doesNotContain(second);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingHero);
    }
}
