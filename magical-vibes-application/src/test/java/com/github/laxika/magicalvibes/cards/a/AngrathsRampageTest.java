package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DavrielRogueShadowmage;
import com.github.laxika.magicalvibes.cards.g.GuildGlobe;
import com.github.laxika.magicalvibes.cards.i.IronBully;
import com.github.laxika.magicalvibes.cards.t.TibaltRakishInstigator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngrathsRampage.class, ArborealGrazer.class, DavrielRogueShadowmage.class, GuildGlobe.class,
        IronBully.class, TibaltRakishInstigator.class})
class AngrathsRampageTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode makes the target player sacrifice an artifact of their choice")
    void artifactModeSacrificesChosenArtifact() {
        Permanent bully = harness.addToBattlefieldAndReturn(player2, new IronBully());
        harness.addToBattlefield(player2, new GuildGlobe());

        cast(0);
        harness.handleMultiplePermanentsChosen(player2, List.of(bully.getId()));

        harness.assertInGraveyard(player2, "Iron Bully");
        harness.assertOnBattlefield(player2, "Guild Globe");
    }

    @Test
    @DisplayName("Creature mode makes the target player sacrifice a creature of their choice")
    void creatureModeSacrificesChosenCreature() {
        Permanent bully = harness.addToBattlefieldAndReturn(player2, new IronBully());
        harness.addToBattlefield(player2, new IronBully());

        cast(1);
        harness.handlePermanentChosen(player2, bully.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof IronBully)
                .hasSize(1);
    }

    @Test
    @DisplayName("Planeswalker mode makes the target player sacrifice a planeswalker")
    void planeswalkerModeSacrificesPlaneswalker() {
        Permanent tibalt = addPlaneswalker(player2, new TibaltRakishInstigator(), 5);

        cast(2);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(tibalt);
    }

    @Test
    @DisplayName("The modes can target any player")
    void modesCanTargetController() {
        harness.addToBattlefield(player1, new GuildGlobe());

        harness.setHand(player1, List.of(new AngrathsRampage()));
        addMana();
        harness.castModalSorcery(player1, 0, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Guild Globe");
    }

    @Test
    @DisplayName("A mode requires a player target")
    void modeCannotTargetPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GuildGlobe());

        harness.setHand(player1, List.of(new AngrathsRampage()));
        addMana();

        UUID artifactId = artifact.getId();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(artifactId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Every mode can target a player with an empty battlefield")
    void modesResolveWithoutPermanents(int modeIndex) {
        harness.addToBattlefield(player1, new IronBully());

        cast(modeIndex);

        harness.assertInGraveyard(player1, "Angrath's Rampage");
        harness.assertOnBattlefield(player1, "Iron Bully");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Creature and planeswalker modes do not sacrifice a noncreature artifact")
    void modesIgnoreNonmatchingArtifacts(int modeIndex) {
        harness.addToBattlefield(player2, new GuildGlobe());

        cast(modeIndex);

        harness.assertOnBattlefield(player2, "Guild Globe");
        harness.assertInGraveyard(player1, "Angrath's Rampage");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Artifact mode does not sacrifice a nonartifact creature")
    void artifactModeIgnoresNonartifactCreature() {
        harness.addToBattlefield(player2, new ArborealGrazer());

        cast(0);

        harness.assertOnBattlefield(player2, "Arboreal Grazer");
        harness.assertInGraveyard(player1, "Angrath's Rampage");
    }

    @Test
    @DisplayName("Creature mode sacrifices an artifact creature without sacrificing other artifacts")
    void creatureModeSacrificesOnlyCreature() {
        harness.addToBattlefield(player2, new IronBully());
        harness.addToBattlefield(player2, new GuildGlobe());

        cast(1);

        harness.assertInGraveyard(player2, "Iron Bully");
        harness.assertOnBattlefield(player2, "Guild Globe");
    }

    @Test
    @DisplayName("The target player chooses which planeswalker to sacrifice")
    void planeswalkerModeSacrificesChosenPlaneswalker() {
        Permanent davriel = addPlaneswalker(player2, new DavrielRogueShadowmage(), 3);
        addPlaneswalker(player2, new TibaltRakishInstigator(), 5);
        harness.addToBattlefield(player2, new IronBully());

        cast(2);
        harness.handleMultiplePermanentsChosen(player2, List.of(davriel.getId()));

        harness.assertInGraveyard(player2, "Davriel, Rogue Shadowmage");
        harness.assertOnBattlefield(player2, "Tibalt, Rakish Instigator");
        harness.assertOnBattlefield(player2, "Iron Bully");
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new AngrathsRampage()));
        addMana();
        harness.castModalSorcery(player1, 0, modeIndex, List.of(player2.getId()));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player,
                                      Card planeswalkerCard, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, planeswalkerCard);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }
}
