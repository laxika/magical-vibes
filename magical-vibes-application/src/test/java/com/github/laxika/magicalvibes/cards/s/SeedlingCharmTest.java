package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.m.MtendaLion;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeedlingCharm.class, MtendaLion.class, FeralShadow.class, Pacifism.class})
class SeedlingCharmTest extends BaseCardTest {

    @Nested
    @CardUsed({SeedlingCharm.class, MtendaLion.class, FeralShadow.class, Pacifism.class})
    @DisplayName("Mode 0: Return target Aura attached to a creature to its owner's hand")
    class BounceAuraMode {

        @Test
        @DisplayName("Returns the targeted Aura to its owner's hand")
        void returnsAura() {
            harness.addToBattlefield(player2, new MtendaLion());
            Permanent host = findPermanent(player2, "Mtenda Lion");
            Permanent aura = addAuraAttachedTo(player2, host);
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castInstant(player1, 0, 0, aura.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);
            harness.assertInHand(player2, "Pacifism");
        }

        @Test
        void returnsAuraToOwnerRatherThanController() {
            Permanent host = harness.addToBattlefieldAndReturn(player2, new MtendaLion());
            Permanent aura = addAuraAttachedTo(player2, host);
            gd.stolenCreatures.put(aura.getId(), player1.getId());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castInstant(player1, 0, 0, aura.getId());
            harness.passBothPriorities();

            harness.assertInHand(player1, "Pacifism");
            harness.assertNotInHand(player2, "Pacifism");
            harness.assertOnBattlefield(player2, "Mtenda Lion");
            harness.assertNotOnBattlefield(player2, "Pacifism");
        }

        @Test
        @DisplayName("Cannot target a creature")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new MtendaLion());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID lionId = harness.getPermanentId(player2, "Mtenda Lion");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, lionId))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target an unattached Aura")
        void cannotTargetUnattachedAura() {
            Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, aura.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({SeedlingCharm.class, MtendaLion.class, FeralShadow.class, Pacifism.class})
    @DisplayName("Mode 1: Regenerate target green creature")
    class RegenerateMode {

        @Test
        @DisplayName("Grants a regeneration shield to a green creature")
        void grantsShield() {
            harness.addToBattlefield(player1, new MtendaLion());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID lionId = harness.getPermanentId(player1, "Mtenda Lion");
            harness.castInstant(player1, 0, 1, lionId);
            harness.passBothPriorities();

            assertThat(gqs.findPermanentById(gd, lionId).getRegenerationShield()).isEqualTo(1);
        }

        @Test
        void canRegenerateOpponentsCreatureWithoutImmediatelyTappingIt() {
            Permanent lion = harness.addToBattlefieldAndReturn(player2, new MtendaLion());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castInstant(player1, 0, 1, lion.getId());
            harness.passBothPriorities();

            assertThat(lion.getRegenerationShield()).isEqualTo(1);
            assertThat(lion.isTapped()).isFalse();
        }

        @Test
        void unusedRegenerationShieldExpiresAtEndOfTurn() {
            Permanent lion = harness.addToBattlefieldAndReturn(player1, new MtendaLion());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castInstant(player1, 0, 1, lion.getId());
            harness.passBothPriorities();
            assertThat(lion.getRegenerationShield()).isEqualTo(1);

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(lion.getRegenerationShield()).isZero();
        }

        @Test
        @DisplayName("Regeneration shield saves the green creature from lethal damage")
        void regenerationShieldSavesFromLethalDamage() {
            Permanent lion = harness.addToBattlefieldAndReturn(player1, new MtendaLion());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castInstant(player1, 0, 1, lion.getId());
            harness.passBothPriorities();

            lion.setMarkedDamage(gqs.getEffectiveToughness(gd, lion));
            harness.runStateBasedActions();

            assertThat(gd.playerBattlefields.get(player1.getId())).contains(lion);
            assertThat(lion.getRegenerationShield()).isZero();
            assertThat(lion.getMarkedDamage()).isZero();
            assertThat(lion.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Cannot target a nongreen creature")
        void cannotTargetNonGreenCreature() {
            harness.addToBattlefield(player2, new FeralShadow());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID shadowId = harness.getPermanentId(player2, "Feral Shadow");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, shadowId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({SeedlingCharm.class, MtendaLion.class, FeralShadow.class, Pacifism.class})
    @DisplayName("Mode 2: Target creature gains trample until end of turn")
    class TrampleMode {

        @Test
        @DisplayName("Grants trample to the target creature")
        void grantsTrample() {
            harness.addToBattlefield(player1, new MtendaLion());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID targetId = harness.getPermanentId(player1, "Mtenda Lion");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, gqs.findPermanentById(gd, targetId), Keyword.TRAMPLE)).isTrue();
        }

        @Test
        @DisplayName("Trample wears off at end of turn")
        void trampleWearsOff() {
            harness.addToBattlefield(player1, new MtendaLion());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID targetId = harness.getPermanentId(player1, "Mtenda Lion");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, gqs.findPermanentById(gd, targetId), Keyword.TRAMPLE)).isFalse();
        }

        @Test
        @DisplayName("Can target a nongreen creature")
        void canTargetNonGreenCreature() {
            harness.addToBattlefield(player2, new FeralShadow());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID shadowId = harness.getPermanentId(player2, "Feral Shadow");
            harness.castInstant(player1, 0, 2, shadowId);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, gqs.findPermanentById(gd, shadowId), Keyword.TRAMPLE)).isTrue();
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNonCreature() {
            Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
            harness.setHand(player1, List.of(new SeedlingCharm()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, aura.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private Permanent addAuraAttachedTo(Player player, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, new Pacifism());
        aura.setAttachedTo(host.getId());
        return aura;
    }

}
