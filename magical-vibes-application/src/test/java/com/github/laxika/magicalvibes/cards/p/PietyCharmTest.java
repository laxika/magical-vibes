package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({PietyCharm.class, ElvishWarrior.class, GlorySeeker.class, Pacifism.class, Plains.class})
class PietyCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target Aura attached to a creature")
    class DestroyAuraMode {

        @Test
        @DisplayName("Destroys an Aura attached to a creature")
        void destroysAttachedAura() {
            Permanent host = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
            Permanent aura = addAuraAttachedTo(host);
            castCharm(0, aura.getId());

            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);
            harness.assertInGraveyard(player2, "Pacifism");
        }

        @Test
        @DisplayName("Cannot target an Aura attached to a noncreature")
        void rejectsAuraAttachedToNoncreature() {
            Permanent host = harness.addToBattlefieldAndReturn(player2, new Plains());
            Permanent aura = addAuraAttachedTo(host);

            assertThatThrownBy(() -> castCharm(0, aura.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Can destroy an Aura controlled by the caster on an opponent's creature")
        void destroysAuraRegardlessOfAuraController() {
            Permanent host = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
            Permanent aura = addAuraAttachedTo(player1, host);
            castCharm(0, aura.getId());

            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
            harness.assertInGraveyard(player1, "Pacifism");
        }
    }

    @Nested
    @DisplayName("Mode 1: Target Soldier creature gets +2/+2 until end of turn")
    class SoldierBoostMode {

        @Test
        @DisplayName("Boosts the targeted Soldier creature")
        void boostsSoldier() {
            Permanent soldier = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
            castCharm(1, soldier.getId());

            assertThat(soldier.getPowerModifier()).isEqualTo(2);
            assertThat(soldier.getToughnessModifier()).isEqualTo(2);
        }

        @Test
        @DisplayName("Cannot target a non-Soldier creature")
        void rejectsNonSoldier() {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

            assertThatThrownBy(() -> castCharm(1, creature.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Boosts a Soldier regardless of its controller")
        void boostsOpponentsSoldier() {
            Permanent soldier = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
            castCharm(1, soldier.getId());

            assertThat(soldier.getPowerModifier()).isEqualTo(2);
            assertThat(soldier.getToughnessModifier()).isEqualTo(2);
        }

        @Test
        @DisplayName("The boost wears off at end of turn")
        void boostWearsOffAtEndOfTurn() {
            Permanent soldier = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
            castCharm(1, soldier.getId());

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(soldier.getPowerModifier()).isZero();
            assertThat(soldier.getToughnessModifier()).isZero();
        }
    }

    @Nested
    @DisplayName("Mode 2: Creatures you control gain vigilance until end of turn")
    class VigilanceMode {

        @Test
        @DisplayName("Grants vigilance to your creatures only")
        void grantsVigilanceToOwnCreatures() {
            Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
            Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
            castCharm(2, null);

            assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
        }

        @Test
        @DisplayName("Vigilance wears off at end of turn")
        void vigilanceWearsOffAtEndOfTurn() {
            Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
            castCharm(2, null);

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isFalse();
        }

        @Test
        @DisplayName("Does not grant vigilance to creatures that enter later")
        void laterCreaturesDoNotGainVigilance() {
            Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
            castCharm(2, null);

            Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

            assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.VIGILANCE)).isFalse();
        }
    }

    private Permanent addAuraAttachedTo(Permanent host) {
        return addAuraAttachedTo(player2, host);
    }

    private Permanent addAuraAttachedTo(com.github.laxika.magicalvibes.model.Player controller,
                                        Permanent host) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void castCharm(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new PietyCharm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalInstant(player1, 0, mode,
                targetId == null ? List.of() : List.of(targetId));
        harness.passBothPriorities();
    }
}
