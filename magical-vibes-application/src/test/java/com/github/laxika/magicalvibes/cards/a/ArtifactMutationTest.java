package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({ArtifactMutation.class, AlloyGolem.class, AncientKavu.class})
class ArtifactMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the artifact and creates Saprolings equal to its mana value")
    void destroysArtifactAndCreatesSaprolings() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlloyGolem());
        harness.setHand(player1, List.of(new ArtifactMutation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Alloy Golem");
        harness.assertInGraveyard(player2, "Alloy Golem");
        List<Permanent> saprolings = findPermanents(player1, "Saproling");
        assertThat(saprolings).hasSize(6);
        assertThat(saprolings).allSatisfy(saproling -> {
            assertThat(saproling.getCard().isToken()).isTrue();
            assertThat(saproling.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(saproling.getCard().getPower()).isEqualTo(1);
            assertThat(saproling.getCard().getToughness()).isEqualTo(1);
            assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        });
    }

    @Test
    @DisplayName("Cannot be regenerated when destroying the artifact")
    void cannotBeRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlloyGolem());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new ArtifactMutation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Alloy Golem");
        harness.assertInGraveyard(player2, "Alloy Golem");
    }

    @Test
    @DisplayName("Creates Saprolings even when the artifact is indestructible")
    void createsSaprolingsWhenArtifactIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlloyGolem());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new ArtifactMutation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Alloy Golem");
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientKavu());
        harness.setHand(player1, List.of(new ArtifactMutation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
