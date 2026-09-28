package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cybership.class, GrizzlyBears.class, Forest.class})
class CybershipTest extends BaseCardTest {

    @Test
    void combatDamagePutsTopTwoCardsOntoBattlefieldAsCybermen() {
        Permanent cybership = addCybershipReady();
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Forest();
        Card remainingCard = new Forest();
        harness.setLibrary(player2, List.of(firstCard, secondCard, remainingCard));

        cybership.setAnimatedUntilEndOfTurn(true);
        cybership.setAnimatedPower(8);
        cybership.setAnimatedToughness(8);
        cybership.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        List<Permanent> cybermen = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isFaceDown()
                        && (permanent.getCard().getId().equals(firstCard.getId())
                        || permanent.getCard().getId().equals(secondCard.getId())))
                .toList();
        assertThat(cybermen).hasSize(2);
        assertThat(cybermen).allSatisfy(cyberman -> {
            assertThat(cyberman.getFaceDownPower()).isEqualTo(2);
            assertThat(cyberman.getFaceDownToughness()).isEqualTo(2);
            assertThat(gqs.getEffectiveCardTypes(gd, cyberman))
                    .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
            assertThat(gqs.effectiveCreatureSubtypes(gd, cyberman))
                    .containsExactly(CardSubtype.CYBERMAN);
        });
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
    }

    @Test
    void putsOnlyAvailableCardsOntoBattlefield() {
        Permanent cybership = addCybershipReady();
        Card onlyCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(onlyCard));

        cybership.setAnimatedUntilEndOfTurn(true);
        cybership.setAnimatedPower(8);
        cybership.setAnimatedToughness(8);
        cybership.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isFaceDown()
                        && permanent.getCard().getId().equals(onlyCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private Permanent addCybershipReady() {
        Permanent cybership = new Permanent(new Cybership());
        cybership.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cybership);
        return cybership;
    }
}
