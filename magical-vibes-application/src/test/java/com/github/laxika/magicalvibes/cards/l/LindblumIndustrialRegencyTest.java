package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MageSiege;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LindblumIndustrialRegency.class, MageSiege.class, Shock.class})
class LindblumIndustrialRegencyTest extends BaseCardTest {

    @Test
    void entersTappedAndProducesRedMana() {
        harness.setHand(player1, List.of(new LindblumIndustrialRegency()));

        harness.playLand(player1, 0);
        Permanent lindblum = findPermanent(player1, "Lindblum, Industrial Regency");
        assertThat(lindblum.isTapped()).isTrue();

        lindblum.untap();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void adventureCreatesWizardAndTheTokenDamagesEachOpponentForNoncreatureSpells() {
        LindblumIndustrialRegency lindblum = new LindblumIndustrialRegency();
        harness.setHand(player1, List.of(lindblum, new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playCardWithAdventure(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(lindblum.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getPower() == 0
                        && permanent.getCard().getToughness() == 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}
