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
import java.util.stream.IntStream;

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
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, NO_MORE, List.of(opponent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fusePhasesOutCreaturesBeforeLethalDamageIsChecked() {
        Permanent saved = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new GallifreyFallsNoMore()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castModalInstant(player1, 0, FUSE, List.of(saved.getId()));
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(saved);
        harness.assertNotInGraveyard(player1, "Fugitive Wizard");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fugitive Wizard"));
    }

    @Test
    void noMoreCanBeCastWithZeroTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new GallifreyFallsNoMore()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castModalInstant(player1, 0, NO_MORE, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Gallifrey Falls // No More");
    }

    @Test
    void noMoreCanTargetMoreThanNinetyNineCreatures() {
        List<Permanent> creatures = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new AirElemental()))
                .toList();
        harness.setHand(player1, List.of(new GallifreyFallsNoMore()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castModalInstant(player1, 0, NO_MORE,
                creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).containsAll(creatures);
    }

    @Test
    void fuseWithZeroTargetsStillDealsDamage() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new GallifreyFallsNoMore()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castModalInstant(player1, 0, FUSE, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Air Elemental"));
    }

    @Test
    void fuseCannotBeCastFromExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        GallifreyFallsNoMore card = new GallifreyFallsNoMore();
        harness.setExile(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.getSpellCastingService()
                .playCardFromExileAsResolutionCast(gd, player1, card.getId(), FUSE, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
