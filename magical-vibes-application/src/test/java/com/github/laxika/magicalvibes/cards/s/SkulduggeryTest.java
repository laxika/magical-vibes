package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostFirstTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSecondTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkulduggeryTest extends BaseCardTest {

    @Test
    @DisplayName("Card has BoostFirstTargetCreatureEffect(+1/+1) and BoostSecondTargetCreatureEffect(-1/-1)")
    void cardHasCorrectEffects() {
        Skulduggery card = new Skulduggery();
        var effects = card.getEffects(EffectSlot.SPELL);
        assertThat(effects).hasSize(2);
        assertThat(effects.get(0)).isInstanceOf(BoostFirstTargetCreatureEffect.class);
        assertThat(effects.get(1)).isInstanceOf(BoostSecondTargetCreatureEffect.class);

        BoostFirstTargetCreatureEffect boost = (BoostFirstTargetCreatureEffect) effects.get(0);
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);

        BoostSecondTargetCreatureEffect debuff = (BoostSecondTargetCreatureEffect) effects.get(1);
        assertThat(debuff.powerBoost()).isEqualTo(-1);
        assertThat(debuff.toughnessBoost()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Target creature you control gets +1/+1 and target opponent creature gets -1/-1")
    void boostsOwnAndDebuffsOpponent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID oppId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(ownId, oppId));
        harness.passBothPriorities();

        Permanent own = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(own.getPowerModifier()).isEqualTo(1);
        assertThat(own.getToughnessModifier()).isEqualTo(1);

        Permanent opp = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(opp.getPowerModifier()).isEqualTo(-1);
        assertThat(opp.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("-1/-1 kills a 1/1 creature")
    void debuffKillsOneOneCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID oppId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(ownId, oppId));
        harness.passBothPriorities();

        Permanent own = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(own.getPowerModifier()).isEqualTo(1);
        assertThat(own.getToughnessModifier()).isEqualTo(1);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target opponent's creature as first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player2, bear2);
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target own creature as second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1);
        harness.addToBattlefield(player1, bear2);
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spell fizzles when all targets removed before resolution")
    void fizzlesWhenAllTargetsRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID oppId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(ownId, oppId));

        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Boost still applies when second target removed before resolution")
    void boostAppliesWhenSecondTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID oppId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(ownId, oppId));

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        Permanent own = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(own.getPowerModifier()).isEqualTo(1);
        assertThat(own.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debuff still applies when first target removed before resolution")
    void debuffAppliesWhenFirstTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skulduggery()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID oppId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(ownId, oppId));

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        Permanent opp = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(opp.getPowerModifier()).isEqualTo(-1);
        assertThat(opp.getToughnessModifier()).isEqualTo(-1);
    }
}
