package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntingCheetah.class, Forest.class, ShuFootSoldiers.class, HermeticStudy.class})
class HuntingCheetahTest extends BaseCardTest {

    private List<Forest> setupLibrary() {
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        harness.setLibrary(player1, List.of(firstForest, secondForest,
                new ShuFootSoldiers(), new ShuFootSoldiers()));
        return List.of(firstForest, secondForest);
    }

    @Test
    @DisplayName("Dealing combat damage to a player presents the may prompt")
    void combatDamagePresentsMayChoice() {
        addCreatureReady(player1, new HuntingCheetah());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may prompt searches the library for Forest cards only")
    void acceptingInitiatesForestSearch() {
        List<Forest> forests = setupLibrary();
        addCreatureReady(player1, new HuntingCheetah());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactlyElementsOf(forests);
    }

    @Test
    @DisplayName("Choosing a Forest puts it into hand")
    void choosingForestPutsItIntoHand() {
        List<Forest> forests = setupLibrary();
        addCreatureReady(player1, new HuntingCheetah());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(forests.get(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Declining the may prompt does not search")
    void decliningSkipsSearch() {
        setupLibrary();
        addCreatureReady(player1, new HuntingCheetah());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No trigger when blocked and no damage reaches the player")
    void noTriggerWhenBlocked() {
        addCreatureReady(player1, new HuntingCheetah());
        addCreatureReady(player2, new ShuFootSoldiers());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Noncombat damage to an opponent also presents the may search choice")
    void noncombatDamageToOpponentPresentsMayChoice() {
        List<Forest> forests = setupLibrary();
        Permanent cheetah = addCreatureReady(player1, new HuntingCheetah());
        attachHermeticStudy(cheetah);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactlyElementsOf(forests);
    }

    @Test
    @DisplayName("Damage to its controller does not present the may search choice")
    void damageToControllerDoesNotTriggerSearch() {
        harness.setLife(player1, 20);
        setupLibrary();
        Permanent cheetah = addCreatureReady(player1, new HuntingCheetah());
        attachHermeticStudy(cheetah);

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    private void attachHermeticStudy(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(creature.getId());
    }
}
