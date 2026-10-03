package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AsgardianCitadel.class)
class AsgardianCitadelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new AsgardianCitadel()));

        harness.playLand(player1, 0);

        Permanent citadel = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(citadel.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability adds red mana when red is chosen")
    void manaAbilityAddsRedMana() {
        Permanent citadel = addReadyCitadel();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(citadel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds white mana when white is chosen")
    void manaAbilityAddsWhiteMana() {
        Permanent citadel = addReadyCitadel();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(citadel.isTapped()).isTrue();
    }

    private Permanent addReadyCitadel() {
        Permanent citadel = harness.addToBattlefieldAndReturn(player1, new AsgardianCitadel());
        citadel.setSummoningSick(false);
        return citadel;
    }

    @Test
    @DisplayName("A tapped Citadel cannot activate its mana ability")
    void tappedCitadelCannotProduceMana() {
        Permanent citadel = addReadyCitadel();
        citadel.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Entry life gain uses the stack and survives the land leaving")
    void entryTriggerSurvivesSourceLeaving() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AsgardianCitadel()));

        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        Permanent citadel = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(citadel);
        gd.playerGraveyards.get(player1.getId()).add(citadel.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
