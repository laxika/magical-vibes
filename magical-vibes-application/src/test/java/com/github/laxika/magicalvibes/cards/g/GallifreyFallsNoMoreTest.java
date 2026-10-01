package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GallifreyFallsNoMore.class, AirElemental.class, ColossalDreadmaw.class, FugitiveWizard.class})
class GallifreyFallsNoMoreTest extends BaseCardTest {

    private static final int GALLIFREY_FALLS = 0;
    private static final int NO_MORE = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Gallifrey Falls deals four damage and exiles creatures it kills")
    void gallifreyFallsDealsDamageAndExilesKilledCreatures() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new GallifreyFallsNoMore()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castModalInstant(player1, 0, GALLIFREY_FALLS, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotInGraveyard(player1, "Fugitive Wizard");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Fugitive Wizard"));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivor);
    }

    @Test
    @DisplayName("No More phases out any number of target creatures you control")
    void noMorePhasesOutMultipleControlledCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new GallifreyFallsNoMore()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castModalInstant(player1, 0, NO_MORE, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(first, second);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
    }

    @Test
    @DisplayName("Fuse resolves Gallifrey Falls before No More")
    void fuseResolvesBothHalves() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent phasedOut = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new GallifreyFallsNoMore()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castModalInstant(player1, 0, FUSE, List.of(phasedOut.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Fugitive Wizard"));
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(phasedOut);
    }

    @Test
    @DisplayName("No More cannot target an opponent's creature")
    void noMoreCannotTargetOpponentsCreature() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new GallifreyFallsNoMore()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, NO_MORE, List.of(opponent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
