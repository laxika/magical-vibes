package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OranRiefSurvivalist;
import com.github.laxika.magicalvibes.cards.v.VampireLacerator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CryptOfAgadeem.class, VampireLacerator.class, OranRiefSurvivalist.class,
        Mountain.class, Disfigure.class})
class CryptOfAgadeemTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new CryptOfAgadeem()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Crypt of Agadeem").isTapped()).isTrue();
    }

    @Test
    @DisplayName("First ability adds one black mana")
    void firstAbilityAddsBlackMana() {
        addCreatureReady(player1, new CryptOfAgadeem());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds black mana for each black creature card in the controller's graveyard")
    void secondAbilityAddsBlackManaForBlackCreatures() {
        harness.setGraveyard(player1, List.of(
                new VampireLacerator(),
                new VampireLacerator(),
                new OranRiefSurvivalist(),
                new Mountain()
        ));
        addCreatureReady(player1, new CryptOfAgadeem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability ignores nonblack creatures, noncreatures, and the opponent's graveyard")
    void secondAbilityCountsOnlyOwnBlackCreatureCards() {
        harness.setGraveyard(player1, List.of(new VampireLacerator(), new OranRiefSurvivalist(), new Mountain()));
        harness.setGraveyard(player2, List.of(new VampireLacerator(), new VampireLacerator()));
        addCreatureReady(player1, new CryptOfAgadeem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability pays two mana and taps even when no black creatures are present")
    void secondAbilityWithNoBlackCreaturesProducesNoMana() {
        harness.setGraveyard(player1, List.of(new Disfigure(), new OranRiefSurvivalist(), new Mountain()));
        Permanent crypt = harness.addToBattlefieldAndReturn(player1, new CryptOfAgadeem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(crypt.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Second ability excludes black noncreature cards and pays its generic mana cost")
    void secondAbilityExcludesBlackNoncreatures() {
        harness.setGraveyard(player1, List.of(new VampireLacerator(), new Disfigure()));
        Permanent crypt = harness.addToBattlefieldAndReturn(player1, new CryptOfAgadeem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(crypt.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Second ability cannot use the mana it would produce to pay its own cost")
    void secondAbilityRequiresTwoManaBeforeActivation() {
        harness.setGraveyard(player1, List.of(new VampireLacerator(), new VampireLacerator()));
        Permanent crypt = harness.addToBattlefieldAndReturn(player1, new CryptOfAgadeem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(crypt.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Neither mana ability can be activated while the land is tapped")
    void tappedLandCannotActivateEitherAbility() {
        harness.setHand(player1, List.of(new CryptOfAgadeem()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
