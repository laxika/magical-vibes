package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BastionMastodon;
import com.github.laxika.magicalvibes.cards.b.BlightsteelColossus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fumigate.class, GrizzlyBears.class, BlightsteelColossus.class,
        BastionMastodon.class, PropheticPrism.class})
class FumigateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and gains one life for each creature destroyed")
    void destroysAllCreaturesAndGainsLifeForEachDestroyed() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Fumigate()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Does not destroy indestructible creatures or gain life for them")
    void doesNotDestroyIndestructibleCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BlightsteelColossus());
        harness.setHand(player1, List.of(new Fumigate()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Blightsteel Colossus");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Gains no life when only noncreature artifacts are on the battlefield")
    void noCreaturesDestroyedGainsNoLife() {
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new Fumigate()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Prophetic Prism");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Fumigate");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Destroys artifact creatures but leaves noncreature artifacts intact")
    void destroysArtifactCreaturesOnly() {
        harness.addToBattlefield(player1, new BastionMastodon());
        harness.addToBattlefield(player2, new BastionMastodon());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new Fumigate()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Bastion Mastodon");
        harness.assertNotOnBattlefield(player2, "Bastion Mastodon");
        harness.assertInGraveyard(player1, "Bastion Mastodon");
        harness.assertInGraveyard(player2, "Bastion Mastodon");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Regenerated creatures survive and do not contribute to life gained")
    void regeneratedCreatureDoesNotCountAsDestroyed() {
        harness.addToBattlefield(player1, new BastionMastodon());
        harness.addToBattlefield(player2, new BastionMastodon());
        Permanent regenerated = findPermanent(player2, "Bastion Mastodon");
        regenerated.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Fumigate()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Bastion Mastodon");
        harness.assertOnBattlefield(player2, "Bastion Mastodon");
        harness.assertNotInGraveyard(player2, "Bastion Mastodon");
        assertThat(regenerated.isTapped()).isTrue();
        assertThat(regenerated.getRegenerationShield()).isZero();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }
}
