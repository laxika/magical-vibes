package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GloriousDecayTest extends BaseCardTest {

    private void addGeneric() {
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

    

    @Nested
    @DisplayName("Mode 0: Destroy target artifact")
    class DestroyArtifactMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new FountainOfYouth());
            harness.setHand(player1, List.of(new GloriousDecay()));
            addGeneric();

            UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Fountain of Youth"));
        }

        @Test
        @DisplayName("Cannot target a non-artifact")
        void cannotTargetNonArtifact() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player1, new FountainOfYouth());
            harness.setHand(player1, List.of(new GloriousDecay()));
            addGeneric();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: 4 damage to target creature with flying")
    class DamageFlyingMode {

        @Test
        @DisplayName("Deals 4 damage to a flying creature")
        void damagesFlyingCreature() {
            harness.addToBattlefield(player2, new AirElemental());
            harness.setHand(player1, List.of(new GloriousDecay()));
            addGeneric();

            UUID targetId = harness.getPermanentId(player2, "Air Elemental");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Air Elemental"));
        }

        @Test
        @DisplayName("Cannot target a creature without flying")
        void cannotTargetNonFlyer() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player1, new AirElemental());
            harness.setHand(player1, List.of(new GloriousDecay()));
            addGeneric();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Exile target card from a graveyard, draw a card")
    class ExileGraveyardMode {

        @Test
        @DisplayName("Exiles a graveyard card and draws a card")
        void exilesAndDraws() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
            harness.setHand(player1, List.of(new GloriousDecay()));
            harness.setLibrary(player1, List.of(new GrizzlyBears()));
            addGeneric();

            harness.castInstant(player1, 0, 2, bears.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.getPlayerExiledCards(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        }
    }
}
