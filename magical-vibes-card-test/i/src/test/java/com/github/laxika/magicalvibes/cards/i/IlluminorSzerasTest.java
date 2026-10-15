package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NecronDeathmark;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IlluminorSzeras.class, GrizzlyBears.class, NecronDeathmark.class})
class IlluminorSzerasTest extends BaseCardTest {

    @Test
    void sacrificesAnotherCreatureAndAddsBlackManaEqualToItsManaValue() {
        Permanent szeras = addCreatureReady(player1, new IlluminorSzeras());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(szeras.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    void cannotSacrificeIlluminorSzerasItself() {
        Permanent szeras = addCreatureReady(player1, new IlluminorSzeras());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(szeras.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    void tappedSacrificedCreatureStillProducesItsManaValueImmediately() {
        Permanent szeras = addCreatureReady(player1, new IlluminorSzeras());
        Permanent deathmark = addCreatureReady(player1, new NecronDeathmark());
        deathmark.tap();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Necron Deathmark");
        assertThat(szeras.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chosenCreatureDeterminesManaWhenMultipleSacrificesAreAvailable() {
        addCreatureReady(player1, new IlluminorSzeras());
        Permanent first = addCreatureReady(player1, new NecronDeathmark());
        Permanent chosen = addCreatureReady(player1, new NecronDeathmark());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(chosen);
        harness.assertInGraveyard(player1, "Necron Deathmark");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeAnOpponentsCreature() {
        Permanent szeras = addCreatureReady(player1, new IlluminorSzeras());
        Permanent opponentCreature = addCreatureReady(player2, new NecronDeathmark());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(szeras.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void summoningSickSzerasCannotActivateItsTapAbility() {
        Permanent szeras = harness.addToBattlefieldAndReturn(player1, new IlluminorSzeras());
        szeras.setSummoningSick(true);
        Permanent sacrifice = addCreatureReady(player1, new NecronDeathmark());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(szeras.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void tappedSzerasCannotActivateAgain() {
        Permanent szeras = addCreatureReady(player1, new IlluminorSzeras());
        szeras.tap();
        Permanent sacrifice = addCreatureReady(player1, new NecronDeathmark());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
