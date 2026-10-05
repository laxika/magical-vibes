package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AltarOfThePantheon;
import com.github.laxika.magicalvibes.cards.e.ElvishArchdruid;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NyxLotus.class, ElvishArchdruid.class, LlanowarElves.class, AltarOfThePantheon.class})
class NyxLotusTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldTapped() {
        harness.setHand(player1, List.of(new NyxLotus()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void addsManaEqualToChosenColorDevotion() {
        harness.addToBattlefield(player1, new NyxLotus());
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new ElvishArchdruid());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void choosingAColorWithoutDevotionAddsNoMana() {
        harness.addToBattlefield(player1, new NyxLotus());
        harness.addToBattlefield(player1, new ElvishArchdruid());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void noncreatureCanActivateImmediatelyAfterEnteringOnceUntapped() {
        var lotus = harness.enterBattlefieldAndReturn(player1, new NyxLotus());
        harness.addToBattlefield(player1, new LlanowarElves());
        assertThat(lotus.isTapped()).isTrue();
        lotus.untap();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(lotus.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedPermanentsCountButCardsOutsideBattlefieldDoNot() {
        harness.addToBattlefield(player1, new NyxLotus());
        harness.addToBattlefieldAndReturn(player1, new LlanowarElves()).setTapped(true);
        harness.setHand(player1, List.of(new ElvishArchdruid()));
        harness.setGraveyard(player1, List.of(new ElvishArchdruid()));
        harness.setExile(player1, List.of(new ElvishArchdruid()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void devotionIncreaseCanProduceAnyColorWithoutColoredManaSymbols(ManaColor color) {
        harness.addToBattlefield(player1, new NyxLotus());
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        harness.addToBattlefield(player2, new AltarOfThePantheon());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void faceDownPermanentHasNoManaCostAndContributesNoDevotion() {
        harness.addToBattlefield(player1, new NyxLotus());
        var manifested = harness.addToBattlefieldAndReturn(player1, new ElvishArchdruid());
        manifested.setManifested(true);
        manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
