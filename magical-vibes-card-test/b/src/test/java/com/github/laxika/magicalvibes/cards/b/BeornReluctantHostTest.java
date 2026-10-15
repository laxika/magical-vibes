package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TillAndTend;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeornReluctantHost.class, TillAndTend.class, Forest.class})
class BeornReluctantHostTest extends BaseCardTest {

    @Test
    void adventureGrantsAnAdditionalLandPlayAndExilesBeorn() {
        BeornReluctantHost card = new BeornReluctantHost();
        harness.setHand(player1, List.of(card, new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        BeornReluctantHost card = new BeornReluctantHost();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Beorn, Reluctant Host");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureAllowsOneMoreLandAfterTheNormalLandWasAlreadyPlayed() {
        harness.setHand(player1, List.of(new Forest(), new BeornReluctantHost(), new Forest(), new Forest()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.playLand(player1, 0);

        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(2);
        assertThatThrownBy(() -> harness.playLand(player1, 0)).isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    void additionalLandPermissionsFromTwoAdventuresAccumulate() {
        harness.setHand(player1, List.of(new BeornReluctantHost(), new BeornReluctantHost(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(countPermanents(player1, "Forest")).isEqualTo(3);
        assertThatThrownBy(() -> harness.playLand(player1, 0)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landPermissionExpiresButCreatureCastPermissionSurvivesIntoTheNextTurn() {
        BeornReluctantHost card = new BeornReluctantHost();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Beorn, Reluctant Host");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
    }

    @Test
    void castingCreatureDirectlyDoesNotGrantAnAdditionalLandPlay() {
        harness.castFromHand(player1, new BeornReluctantHost(), "{4}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Beorn, Reluctant Host");
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.playLand(player1, 0);
        assertThatThrownBy(() -> harness.playLand(player1, 0)).isInstanceOf(IllegalStateException.class);
    }
}
