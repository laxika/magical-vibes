package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.d.DrowsingTyrannodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReturnToNature.class, ShortSword.class, GloriousAnthem.class, DrowsingTyrannodon.class})
class ReturnToNatureTest extends BaseCardTest {

    private void setUpSpell() {
        harness.setHand(player1, List.of(new ReturnToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

    @Nested
    @CardUsed({ReturnToNature.class, ShortSword.class, DrowsingTyrannodon.class})
    @DisplayName("Mode 0: Destroy target artifact")
    class DestroyArtifactMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new ShortSword());
            setUpSpell();

            UUID targetId = harness.getPermanentId(player2, "Short Sword");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Short Sword");
        }

        @Test
        @DisplayName("Cannot target a non-artifact")
        void cannotTargetNonArtifact() {
            harness.addToBattlefield(player2, new DrowsingTyrannodon());
            harness.addToBattlefield(player1, new ShortSword());
            setUpSpell();

            UUID targetId = harness.getPermanentId(player2, "Drowsing Tyrannodon");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({ReturnToNature.class, GloriousAnthem.class})
    @DisplayName("Mode 1: Destroy target enchantment")
    class DestroyEnchantmentMode {

        @Test
        @DisplayName("Destroys target enchantment")
        void destroysEnchantment() {
            harness.addToBattlefield(player2, new GloriousAnthem());
            setUpSpell();

            UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        }
    }

    @Test
    @DisplayName("Mode 2: Exiles target card from a graveyard")
    void exilesGraveyardCard() {
        Card target = new DrowsingTyrannodon();
        harness.setGraveyard(player2, List.of(target));
        setUpSpell();

        harness.castInstant(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Drowsing Tyrannodon");
    }

    @Test
    void enchantmentModeCannotTargetArtifact() {
        harness.addToBattlefield(player2, new ShortSword());
        harness.addToBattlefield(player1, new GloriousAnthem());
        setUpSpell();

        UUID targetId = harness.getPermanentId(player2, "Short Sword");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysOwnArtifactWithoutAffectingOtherPermanents() {
        harness.addToBattlefield(player1, new ShortSword());
        harness.addToBattlefield(player2, new GloriousAnthem());
        setUpSpell();

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player1, "Short Sword"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Short Sword");
        harness.assertInGraveyard(player1, "Short Sword");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    void exilesOnlySelectedNoncreatureCardFromOwnGraveyard() {
        Card target = new GloriousAnthem();
        Card other = new DrowsingTyrannodon();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setGraveyard(player2, List.of(new ShortSword()));
        setUpSpell();

        harness.castInstant(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Drowsing Tyrannodon");
        harness.assertInGraveyard(player2, "Short Sword");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card().getId()).isEqualTo(target.getId());
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
        });
    }

    @Test
    void graveyardModeRequiresTarget() {
        harness.setGraveyard(player2, List.of(new DrowsingTyrannodon()));
        setUpSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardModeCannotTargetBattlefieldCard() {
        harness.addToBattlefield(player2, new DrowsingTyrannodon());
        harness.setGraveyard(player2, List.of(new ShortSword()));
        setUpSpell();

        UUID targetId = harness.getPermanentId(player2, "Drowsing Tyrannodon");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardModeDoesNotExileAnotherCardWhenTargetLeaves() {
        Card target = new DrowsingTyrannodon();
        Card other = new GloriousAnthem();
        harness.setGraveyard(player2, List.of(target, other));
        setUpSpell();

        harness.castInstant(player1, 0, 2, target.getId());
        harness.setGraveyard(player2, List.of(other));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Glorious Anthem");
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Return to Nature");
    }
}
