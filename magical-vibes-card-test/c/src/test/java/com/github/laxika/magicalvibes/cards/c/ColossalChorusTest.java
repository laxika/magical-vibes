package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LegionsChant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColossalChorus.class, ColossalDreadmaw.class, LegionsChant.class})
class ColossalChorusTest extends BaseCardTest {

    @Test
    void conjuresDreadmawsEqualToStartingIntensity() {
        ColossalChorus chorus = new ColossalChorus();
        harness.castFromHand(player1, chorus, "{6}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allMatch(permanent -> permanent.getCard() instanceof ColossalDreadmaw);
        assertThat(gd.getCardIntensity(chorus.getId())).isEqualTo(3);
    }

    @Test
    void intensifiesAllOwnedChorusCards() {
        ColossalChorus chorus = new ColossalChorus();
        LegionsChant otherChorus = new LegionsChant();
        harness.setLibrary(player1, List.of(otherChorus));
        harness.castFromHand(player1, chorus, "{6}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(chorus.getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(otherChorus.getId())).isEqualTo(4);
    }

    @Test
    void previouslyIntensifiedChorusConjuresThreeDreadmaws() {
        ColossalChorus first = new ColossalChorus();
        ColossalChorus second = new ColossalChorus();
        harness.setLibrary(player1, List.of(second));

        harness.castFromHand(player1, first, "{6}{G}{G}");
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, second, "{6}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(5)
                .allMatch(permanent -> permanent.getCard() instanceof ColossalDreadmaw);
        assertThat(gd.getCardIntensity(first.getId())).isEqualTo(4);
        assertThat(gd.getCardIntensity(second.getId())).isEqualTo(4);
    }

    @Test
    void intensifiesOwnedCardsInHandGraveyardAndExileButNotOpponentsOrNonChorusCards() {
        ColossalChorus resolving = new ColossalChorus();
        ColossalChorus inHand = new ColossalChorus();
        ColossalChorus inGraveyard = new ColossalChorus();
        ColossalChorus inExile = new ColossalChorus();
        ColossalChorus opponentsChorus = new ColossalChorus();
        ColossalDreadmaw nonChorus = new ColossalDreadmaw();
        harness.setGraveyard(player1, List.of(inGraveyard, nonChorus));
        harness.setExile(player1, List.of(inExile));
        harness.setLibrary(player2, List.of(opponentsChorus));

        harness.castFromHand(player1, resolving, "{6}{G}{G}");
        harness.setHand(player1, List.of(inHand));
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(inHand.getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(inGraveyard.getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(inExile.getId())).isEqualTo(3);
        assertThat(gd.cardIntensities).doesNotContainKeys(opponentsChorus.getId(), nonChorus.getId());
    }

    @Test
    void conjuresOwnedNontokenCardsThatRemainInGraveyardAfterDying() {
        harness.castFromHand(player1, new ColossalChorus(), "{6}{G}{G}");
        harness.passBothPriorities();

        var dreadmaws = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        assertThat(dreadmaws).hasSize(2);
        assertThat(dreadmaws).allSatisfy(permanent -> {
            assertThat(permanent.getCard().isToken()).isFalse();
            assertThat(permanent.getCard().getOwnerId()).isEqualTo(player1.getId());
            permanent.setMarkedDamage(6);
        });
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsAll(dreadmaws.stream().map(permanent -> permanent.getCard()).toList());
    }
}
