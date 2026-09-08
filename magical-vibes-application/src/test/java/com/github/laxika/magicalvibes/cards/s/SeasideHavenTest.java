package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenInitiate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeasideHaven.class, AvenInitiate.class, GrizzlyBears.class})
class SeasideHavenTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        Permanent haven = addReadyHaven(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(haven.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificesBirdAndDrawsCard() {
        addReadyHaven(player1);
        harness.addToBattlefield(player1, new AvenInitiate());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Aven Initiate");
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Aven Initiate");
        harness.assertOnBattlefield(player1, "Seaside Haven");
    }

    @Test
    void drawAbilityCannotSacrificeNonBirdCreature() {
        addReadyHaven(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHaven(Player player) {
        Permanent haven = harness.addToBattlefieldAndReturn(player, new SeasideHaven());
        haven.setSummoningSick(false);
        return haven;
    }
}
