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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(fourth.getId(), nonHero.getId(), spell.getId());
    }
}
