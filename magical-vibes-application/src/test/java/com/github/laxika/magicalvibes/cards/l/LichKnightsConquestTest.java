package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JunglePatrol;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SlumberingKeepguard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LichKnightsConquest.class, JunglePatrol.class, Ornithopter.class, GloriousAnthem.class,
        GrizzlyBears.class, HillGiant.class, ChildOfNight.class, Gingerbrute.class, SlumberingKeepguard.class})
class LichKnightsConquestTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices artifacts, enchantments, and tokens, then returns that many creatures")
    void sacrificesEligiblePermanentsAndReturnsTheSameNumberOfCreatures() {
        Permanent junglePatrol = harness.addToBattlefieldAndReturn(player1, new JunglePatrol());
        junglePatrol.setSummoningSick(false);
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent wood = findPermanent(player1, "Wood");
        List<Card> creatureCards = List.of(new GrizzlyBears(), new HillGiant());
        harness.setGraveyard(player1, creatureCards);
        harness.castFromHand(player1, new LichKnightsConquest(), "{4}{B}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(ornithopter.getId(), anthem.getId(), wood.getId()));

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player1, "Hill Giant")).isEqualTo(1);
        assertThat(findPermanent(player1, "Ornithopter")).isNotNull();
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Wood");
        harness.assertInGraveyard(player1, "Lich-Knights' Conquest");
    }

    @Test
    @DisplayName("Choosing zero sacrifices returns no creatures")
    void choosingZeroSacrificesReturnsNothing() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new LichKnightsConquest(), "{4}{B}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(findPermanent(player1, "Ornithopter")).isSameAs(ornithopter);
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Must choose the full number of creature cards when enough are available")
    void mustChooseTheFullReturnCount() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        List<Card> creatureCards = List.of(
                new GrizzlyBears(), new HillGiant(), new ChildOfNight());
        harness.setGraveyard(player1, creatureCards);
        harness.castFromHand(player1, new LichKnightsConquest(), "{4}{B}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(ornithopter.getId(), anthem.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).mandatory()).isTrue();
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("forced graveyard choice");

        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player1, "Hill Giant")).isEqualTo(1);
        assertThat(countPermanents(player1, "Child of Night")).isZero();
        harness.assertInGraveyard(player1, "Child of Night");
    }

    @Test
    @DisplayName("Sacrificed creature tokens are not creature cards that can be returned")
    void cannotReturnASacrificedCreatureToken() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new JunglePatrol());
        patrol.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent wood = findPermanent(player1, "Wood");

        harness.castFromHand(player1, new LichKnightsConquest(), "{4}{B}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(wood.getId()));

        harness.assertNotOnBattlefield(player1, "Wood");
        harness.assertOnBattlefield(player1, "Jungle Patrol");
        harness.assertInGraveyard(player1, "Lich-Knights' Conquest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolves without returning creatures when no eligible permanents are controlled")
    void noEligiblePermanentsReturnsNothing() {
        harness.addToBattlefield(player1, new SlumberingKeepguard());
        harness.addToBattlefield(player2, new Gingerbrute());
        harness.setGraveyard(player1, List.of(new SlumberingKeepguard()));

        harness.castFromHand(player1, new LichKnightsConquest(), "{4}{B}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Slumbering Keepguard")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Slumbering Keepguard");
        harness.assertOnBattlefield(player2, "Gingerbrute");
        harness.assertInGraveyard(player1, "Lich-Knights' Conquest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's artifact or a nontoken ordinary creature")
    void rejectsIneligibleSacrificesAndAllowsAnotherAnswer() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SlumberingKeepguard());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());

        harness.castFromHand(player1, new LichKnightsConquest(), "{4}{B}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Invalid selection");
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(opposingArtifact.getId())))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Invalid selection");
        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));

        assertThat(countPermanents(player1, "Gingerbrute")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Slumbering Keepguard");
        harness.assertOnBattlefield(player2, "Gingerbrute");
        harness.assertInGraveyard(player1, "Lich-Knights' Conquest");
    }

    @Test
    @DisplayName("Returns only one own creature for one artifact creature sacrificed")
    void oneSacrificeReturnsOneCreatureFromOnlyYourGraveyard() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.setGraveyard(player1, List.of(new SlumberingKeepguard(), new LichKnightsConquest()));
        harness.setGraveyard(player2, List.of(new SlumberingKeepguard()));

        harness.castFromHand(player1, new LichKnightsConquest(), "{4}{B}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Slumbering Keepguard")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Gingerbrute");
        harness.assertInGraveyard(player1, "Gingerbrute");
        harness.assertInGraveyard(player1, "Lich-Knights' Conquest");
        harness.assertInGraveyard(player2, "Slumbering Keepguard");
        harness.assertNotOnBattlefield(player2, "Slumbering Keepguard");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
