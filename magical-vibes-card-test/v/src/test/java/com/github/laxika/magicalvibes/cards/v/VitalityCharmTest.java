package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.d.DaruLancer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VitalityCharm.class, BarkhideMauler.class, DaruLancer.class, ArtificialEvolution.class, Lignify.class, Shock.class})
class VitalityCharmTest extends BaseCardTest {

    @Test
    void createsAnInsectToken() {
        harness.setHand(player1, List.of(new VitalityCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getName()).isEqualTo("Insect");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    void boostsTargetCreatureAndGrantsTrampleUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DaruLancer());
        harness.setHand(player1, List.of(new VitalityCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void regeneratesTargetBeast() {
        Permanent beast = harness.addToBattlefieldAndReturn(player2, new BarkhideMauler());
        harness.setHand(player1, List.of(new VitalityCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 2, beast.getId());
        harness.passBothPriorities();

        assertThat(gd.regenerationShieldCount(beast.getId())).isEqualTo(1);
    }

    @Test
    void regenerationModeCannotTargetNonBeastCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DaruLancer());
        harness.setHand(player1, List.of(new VitalityCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void regenerationPreventsLethalDamageOnlyOnce() {
        Permanent beast = harness.addToBattlefieldAndReturn(player2, new BarkhideMauler());
        harness.setHand(player1, List.of(new VitalityCharm(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, beast.getId());
        harness.passBothPriorities();
        assertThat(beast.isTapped()).isFalse();
        harness.castAndResolveInstant(player1, 0, beast.getId());
        assertThat(beast.getMarkedDamage()).isEqualTo(2);
        harness.castAndResolveInstant(player1, 0, beast.getId());

        harness.assertOnBattlefield(player2, "Barkhide Mauler");
        assertThat(beast.isTapped()).isTrue();
        assertThat(beast.getMarkedDamage()).isZero();
        assertThat(gd.regenerationShieldCount(beast.getId())).isZero();

        harness.castAndResolveInstant(player1, 0, beast.getId());
        harness.castAndResolveInstant(player1, 0, beast.getId());
        harness.assertNotOnBattlefield(player2, "Barkhide Mauler");
        harness.assertInGraveyard(player2, "Barkhide Mauler");
    }

    @Test
    void regenerationDoesNotResolveWhenTargetStopsBeingABeast() {
        Permanent beast = harness.addToBattlefieldAndReturn(player2, new BarkhideMauler());
        harness.setHand(player1, List.of(new VitalityCharm(), new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, 2, beast.getId());
        harness.castAndResolveInstant(player1, 0, beast.getId());
        harness.handleListChoice(player1, "BEAST");
        harness.handleListChoice(player1, "INSECT");
        assertThat(gqs.hasEffectiveSubtype(gd, beast, CardSubtype.BEAST)).isFalse();
        harness.passBothPriorities();

        assertThat(gd.regenerationShieldCount(beast.getId())).isZero();
        harness.assertInGraveyard(player1, "Vitality Charm");
    }

    @Test
    void regenerationCanTargetANoncreatureBeastPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DaruLancer());
        harness.setHand(player1, List.of(new Lignify(), new ArtificialEvolution(), new VitalityCharm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Lignify");
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.handleListChoice(player1, "TREEFOLK");
        harness.handleListChoice(player1, "BEAST");

        assertThat(gqs.hasEffectiveSubtype(gd, aura, CardSubtype.BEAST)).isTrue();
        assertThat(gqs.isCreature(gd, aura)).isFalse();
        harness.castInstant(player1, 0, 2, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.regenerationShieldCount(aura.getId())).isEqualTo(1);
    }
}
