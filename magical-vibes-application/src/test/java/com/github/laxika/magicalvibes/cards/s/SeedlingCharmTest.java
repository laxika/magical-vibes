package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeedlingCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Return target Aura attached to a creature to its owner's hand")
    class BounceAuraMode {

        @Test
        @DisplayName("Returns the targeted Aura to its owner's hand")
        void returnsAura() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent host = findPermanent(player2, "Grizzly Bears");
            Permanent aura = addAuraAttachedTo(player2, host);
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castInstant(player1, 0, 0, aura.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);
        }

        @Test
        @DisplayName("Cannot target a creature")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bearsId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Regenerate target green creature")
    class RegenerateMode {

        @Test
        @DisplayName("Grants a regeneration shield to a green creature")
        void grantsShield() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 1, bearsId);
            harness.passBothPriorities();

            assertThat(permanent(player1.getId(), bearsId).getRegenerationShield()).isEqualTo(1);
        }

        @Test
        @DisplayName("Cannot target a nongreen creature")
        void cannotTargetNonGreenCreature() {
            harness.addToBattlefield(player2, new SavannahLions());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID lionsId = harness.getPermanentId(player2, "Savannah Lions");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, lionsId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target creature gains trample until end of turn")
    class TrampleMode {

        @Test
        @DisplayName("Grants trample to the target creature")
        void grantsTrample() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, permanent(player1.getId(), targetId), Keyword.TRAMPLE)).isTrue();
        }

        @Test
        @DisplayName("Trample wears off at end of turn")
        void trampleWearsOff() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, permanent(player1.getId(), targetId), Keyword.TRAMPLE)).isFalse();
        }

        @Test
        @DisplayName("Can target a nongreen creature")
        void canTargetNonGreenCreature() {
            harness.addToBattlefield(player2, new SavannahLions());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID lionsId = harness.getPermanentId(player2, "Savannah Lions");
            harness.castInstant(player1, 0, 2, lionsId);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, permanent(player2.getId(), lionsId), Keyword.TRAMPLE)).isTrue();
        }
    }

    private Permanent addAuraAttachedTo(Player player, Permanent host) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return aura;
    }

    private Permanent permanent(UUID playerId, UUID id) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getId().equals(id)).findFirst().orElseThrow();
    }
}
