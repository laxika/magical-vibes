package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanishingSlash.class, GloriousAnthem.class, GrizzlyBears.class, LeoninScimitar.class})
class BanishingSlashTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a tapped creature and creates a vigilant Samurai when you control an artifact and enchantment")
    void destroysTappedCreatureAndCreatesSamurai() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        cast(List.of(target.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Samurai")).singleElement().satisfies(samurai -> {
            assertThat(samurai.getCard().getPower()).isEqualTo(2);
            assertThat(samurai.getCard().getToughness()).isEqualTo(2);
            assertThat(samurai.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(samurai.getCard().getKeywords()).contains(Keyword.VIGILANCE);
        });
    }

    @Test
    @DisplayName("Can resolve without choosing a target")
    void resolvesWithoutTarget() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new GloriousAnthem());

        harness.setHand(player1, List.of(new BanishingSlash()));
        addMana();
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Samurai")).hasSize(1);
    }

    @Test
    @DisplayName("Checks the artifact and enchantment condition after destruction")
    void checksConditionAfterDestruction() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new GloriousAnthem());

        cast(List.of(artifact.getId()));

        harness.assertInGraveyard(player1, "Leonin Scimitar");
        assertThat(findPermanents(player1, "Samurai")).isEmpty();
    }

    @Test
    @DisplayName("Rejects an untapped creature target")
    void rejectsUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BanishingSlash()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new BanishingSlash()));
        addMana();
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
