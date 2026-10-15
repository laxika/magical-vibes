package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.d.DuneMover;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VanishIntoEternity.class, GrizzlyBears.class, Plains.class,
        CrawlingChorus.class, DuneMover.class, PropheticPrism.class})
class VanishIntoEternityTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature when the additional cost is paid")
    void exilesCreatureAtIncreasedCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VanishIntoEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Exiles a noncreature nonland permanent for its normal cost")
    void exilesNoncreaturePermanentAtNormalCost() {
        Card artifactCard = new Card();
        artifactCard.setName("Relic");
        artifactCard.setType(CardType.ARTIFACT);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, artifactCard);
        harness.setHand(player1, List.of(new VanishIntoEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.findExiledCard(artifact.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Cannot pay the normal cost when targeting a creature")
    void creatureTargetRequiresAdditionalMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VanishIntoEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new VanishIntoEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("An artifact creature still requires the creature surcharge")
    void artifactCreatureRequiresIncreasedCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DuneMover());
        harness.setHand(player1, List.of(new VanishIntoEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player2, "Dune Mover");
    }

    @Test
    @DisplayName("Can exile your own noncreature permanent without the surcharge")
    void exilesOwnArtifactAtNormalCost() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new VanishIntoEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.findExiledCard(artifact.getOriginalCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Prophetic Prism");
    }

    @Test
    @DisplayName("Exiling a creature does not trigger its dies ability")
    void exileDoesNotTriggerDeathAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawlingChorus());
        harness.setHand(player1, List.of(new VanishIntoEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
