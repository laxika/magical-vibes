package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EntreatTheAngels;
import com.github.laxika.magicalvibes.cards.g.GhostlyFlicker;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeraphSanctuary.class, SeraphOfDawn.class, MoorlandInquisitor.class,
        EntreatTheAngels.class, GhostlyFlicker.class})
class SeraphSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when it enters the battlefield")
    void gainsLifeOnOwnEnter() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SeraphSanctuary()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Gains 1 life whenever an Angel you control enters")
    void gainsLifeWhenAngelEnters() {
        harness.addToBattlefield(player1, new SeraphSanctuary());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new SeraphOfDawn()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);

        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not gain life when a non-Angel creature you control enters")
    void noLifeGainForNonAngel() {
        harness.addToBattlefield(player1, new SeraphSanctuary());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new MoorlandInquisitor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);

        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not gain life when an opponent's Angel enters")
    void noLifeGainForOpponentAngel() {
        harness.addToBattlefield(player1, new SeraphSanctuary());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new SeraphOfDawn()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castCreature(player2, 0);

        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Mana ability taps for {C}")
    void manaAbilityAddsColorless() {
        harness.addToBattlefield(player1, new SeraphSanctuary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each Sanctuary triggers separately for each Angel token entering together")
    void gainsLifeForEachAngelTokenAndEachSanctuary() {
        harness.addToBattlefield(player1, new SeraphSanctuary());
        harness.addToBattlefield(player1, new SeraphSanctuary());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new EntreatTheAngels()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 2);
        resolveAllTriggers();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Angel")).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering Sanctuary does not trigger another Sanctuary")
    void secondSanctuaryOnlyGainsLifeForItsOwnEntry() {
        harness.addToBattlefield(player1, new SeraphSanctuary());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SeraphSanctuary()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Can tap for mana immediately while its entry trigger is still pending")
    void manaAbilityResolvesImmediatelyBeforeEntryTrigger() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SeraphSanctuary()));
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanent(player1, "Seraph Sanctuary").isTapped()).isTrue();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Entry trigger still resolves after Sanctuary leaves and returns")
    void entryTriggerSurvivesFlickeringItsSource() {
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SeraphSanctuary(), new GhostlyFlicker()));
        harness.playLand(player1, 0);
        var sanctuaryId = harness.getPermanentId(player1, "Seraph Sanctuary");
        var inquisitorId = harness.getPermanentId(player1, "Moorland Inquisitor");
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, List.of(sanctuaryId, inquisitorId));
        resolveAllTriggers();

        assertThat(harness.getPermanentId(player1, "Seraph Sanctuary")).isNotEqualTo(sanctuaryId);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
