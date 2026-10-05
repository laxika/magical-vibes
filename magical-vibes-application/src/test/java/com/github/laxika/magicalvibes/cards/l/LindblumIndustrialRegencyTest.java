package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MageSiege;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TravelingChocobo;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LindblumIndustrialRegency.class, MageSiege.class})
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
    @CardUsed({Shock.class})
    void adventureCreatesWizardAndTheTokenDamagesEachOpponentForNoncreatureSpells() {
        LindblumIndustrialRegency lindblum = new LindblumIndustrialRegency();
        harness.setHand(player1, List.of(lindblum, new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, List.of());
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

    @Test
    void adventureDoesNotTriggerItsOwnWizardAndLandCanBePlayedFromExile() {
        LindblumIndustrialRegency lindblum = new LindblumIndustrialRegency();
        harness.setHand(player1, List.of(lindblum));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Wizard")).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.castFromExile(player1, lindblum.getId());

        assertThat(findPermanent(player1, "Lindblum, Industrial Regency").isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Wizard")).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({TravelingChocobo.class})
    void wizardDoesNotTriggerForCreatureSpells() {
        harness.setHand(player1, List.of(new LindblumIndustrialRegency(), new TravelingChocobo()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Traveling Chocobo");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void wizardDoesNotTriggerForOpponentsNoncreatureSpells() {
        harness.setHand(player1, List.of(new LindblumIndustrialRegency()));
        harness.setHand(player2, List.of(new LindblumIndustrialRegency()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.castAdventure(player2, 0, List.of());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Wizard")).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void existingWizardTriggersForAnotherAdventureBeforeNewWizardIsCreated() {
        harness.setHand(player1, List.of(new LindblumIndustrialRegency(), new LindblumIndustrialRegency()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.castAdventure(player1, 0, List.of());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        assertThat(countPermanents(player1, "Wizard")).isEqualTo(1);

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Wizard")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 19);
    }
}
