package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScionSummoner.class})
class ScionSummonerTest extends BaseCardTest {

    @Test
    void enteringBattlefieldCreatesAnEldraziScion() {
        castScionSummoner();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    void scionCanBeSacrificedForColorlessMana() {
        castScionSummoner();

        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, null, null);

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void tappedScionCanProduceManaImmediatelyOnTheTurnItEnters() {
        castScionSummoner();
        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        scion.tap();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);

        harness.activateAbility(player1, scionIndex, null, null);

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Scion Summoner");
    }

    @Test
    void triggerCreatesTokenAfterSummonerLeavesTheBattlefield() {
        harness.castFromHand(player1, new ScionSummoner(), "{2}{G}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        Permanent summoner = findPermanent(player1, "Scion Summoner");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, summoner));

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Scion Summoner");
    }

    @Test
    void tokenIsCreatedForTheSummonersController() {
        harness.enterBattlefieldAndReturn(player2, new ScionSummoner());

        resolveAllTriggers();

        assertThat(findPermanents(player2, "Eldrazi Scion")).hasSize(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    private void castScionSummoner() {
        harness.castFromHand(player1, new ScionSummoner(), "{2}{G}");
        resolveAllTriggers();
    }
}
