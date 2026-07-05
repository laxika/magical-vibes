package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LanternSpirit;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YavimayaSapherd;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RequiemAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Has ally creature dies trigger for non-Spirits creating a white flying Spirit")
    void hasCorrectEffect() {
        RequiemAngel card = new RequiemAngel();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).getFirst())
                .isInstanceOf(TriggeringCardConditionalEffect.class);

        TriggeringCardConditionalEffect conditional =
                (TriggeringCardConditionalEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).getFirst();
        assertThat(conditional.predicate())
                .isEqualTo(new CardNotPredicate(new CardSubtypePredicate(CardSubtype.SPIRIT)));
        assertThat(conditional.wrapped()).isInstanceOf(CreateTokenEffect.class);

        CreateTokenEffect token = (CreateTokenEffect) conditional.wrapped();
        assertThat(token.amount()).isEqualTo(new Fixed(1));
        assertThat(token.tokenName()).isEqualTo("Spirit");
        assertThat(token.power()).isEqualTo(1);
        assertThat(token.toughness()).isEqualTo(1);
        assertThat(token.color()).isEqualTo(CardColor.WHITE);
        assertThat(token.subtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(token.keywords()).containsExactly(Keyword.FLYING);
    }

    @Test
    @DisplayName("When another non-Spirit creature you control dies, creates a 1/1 white flying Spirit")
    void createsSpiritWhenOwnNonSpiritCreatureDies() {
        harness.addToBattlefield(player1, new RequiemAngel());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent spirit = spiritTokens(player1).getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(spirit.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when another Spirit creature you control dies")
    void doesNotTriggerForOwnSpiritCreature() {
        harness.addToBattlefield(player1, new RequiemAngel());
        harness.addToBattlefield(player1, new LanternSpirit());

        killWithShock(player2, player1, "Lantern Spirit");

        assertThat(gd.stack).isEmpty();
        assertThat(spiritTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's non-Spirit creature dies")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new RequiemAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killWithShock(player1, player2, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(spiritTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Triggers when a non-Spirit token creature you control dies")
    void triggersForOwnNonSpiritTokenCreature() {
        harness.addToBattlefield(player1, new RequiemAngel());
        harness.setHand(player1, List.of(new YavimayaSapherd()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        killWithShock(player2, player1, "Saproling");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(spiritTokens(player1)).hasSize(1);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster,
                               com.github.laxika.magicalvibes.model.Player targetController,
                               String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private List<Permanent> spiritTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getName().equals("Spirit"))
                .toList();
    }
}
