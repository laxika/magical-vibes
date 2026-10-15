package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WillScholarOfFrost;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerDrewAtLeastCardsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "156")
public class RowanScholarOfSparks extends Card {

    private static final String EMBLEM_TEXT = "Whenever you cast an instant or sorcery spell, you may pay {2}. If you do, copy that spell. You may choose new targets for the copy.";

    public RowanScholarOfSparks() {
        WillScholarOfFrost backFace = new WillScholarOfFrost();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                instantOrSorcery, 1, CostModificationScope.SELF));

        addActivatedAbility(new ActivatedAbility(
                1,
                List.of(new ConditionalReplacementEffect(
                        new ControllerDrewAtLeastCardsThisTurn(3),
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                        new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT))),
                "+1: Rowan deals 1 damage to each opponent. If you've drawn three or more cards this turn, she deals 3 damage to each opponent instead."));
        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new CreateEmblemEffect(
                        List.of(new CopyControllerCastSpellOnSpellCastEffect(instantOrSorcery, "{2}")),
                        EMBLEM_TEXT)),
                "-4: You get an emblem with \"" + EMBLEM_TEXT + "\""));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Rowan, Scholar of Sparks", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Will, Scholar of Frost", backFace.getEffects(EffectSlot.SPELL)))));
    }

    @Override
    public String getBackFaceClassName() {
        return "WillScholarOfFrost";
    }
}
