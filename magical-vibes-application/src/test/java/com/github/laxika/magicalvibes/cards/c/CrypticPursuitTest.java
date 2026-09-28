package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrypticPursuit.class, LightningBolt.class})
class CrypticPursuitTest extends BaseCardTest {

    @Test
    void castingInstantOrSorceryFromHandManifestsTheTopCard() {
        harness.addToBattlefield(player1, new CrypticPursuit());
        Card topCard = new LightningBolt();
        LightningBolt spell = new LightningBolt();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isFaceDown()
                        && permanent.isManifested()
                        && permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    void faceDownInstantOrSorceryIsExiledAndMayBeCastUntilNextTurn() {
        harness.addToBattlefield(player1, new CrypticPursuit());
        LightningBolt manifestedCard = new LightningBolt();
        harness.setLibrary(player1, List.of(manifestedCard));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
        manifested.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(manifestedCard.getId())).isNotNull();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, manifestedCard.getId(), player2.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(manifestedCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(manifestedCard);
    }
}
