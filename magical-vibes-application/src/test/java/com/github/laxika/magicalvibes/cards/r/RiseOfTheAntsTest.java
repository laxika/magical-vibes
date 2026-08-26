package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RiseOfTheAnts.class)
class RiseOfTheAntsTest extends BaseCardTest {

    @Test
    void createsInsectsAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new RiseOfTheAnts()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        List<Permanent> insects = findPermanents(player1, "Insect");
        assertThat(insects).hasSize(2);
        assertThat(insects).allSatisfy(insect -> {
            assertThat(insect.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(insect.getCard().getSubtypes()).contains(CardSubtype.INSECT);
            assertThat(gqs.getEffectivePower(gd, insect)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, insect)).isEqualTo(3);
        });
    }

    @Test
    void flashbackCreatesInsectsGainsLifeAndExilesSpell() {
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, List.of(new RiseOfTheAnts()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(findPermanents(player1, "Insect")).hasSize(2);
        harness.assertNotInGraveyard(player1, "Rise of the Ants");
        GameData gameData = harness.getGameData();
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rise of the Ants"));
    }
}
