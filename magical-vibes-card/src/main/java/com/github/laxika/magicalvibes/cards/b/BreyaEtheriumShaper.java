package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "454")
@CardRegistration(set = "2XM", collectorNumber = "192")
public class BreyaEtheriumShaper extends Card {

    public BreyaEtheriumShaper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Thopter", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING),
                        Set.of(CardType.ARTIFACT)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsArtifactPredicate()),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Breya deals 3 damage to target player or planeswalker",
                                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(3),
                                        new AnyTargetPredicateTargetFilter(
                                                new PermanentIsPlaneswalkerPredicate(),
                                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                                "Target must be a player or planeswalker.")),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Target creature gets -4/-4 until end of turn",
                                        new BoostTargetCreatureEffect(-4, -4),
                                        new PermanentPredicateTargetFilter(
                                                new PermanentIsCreaturePredicate(),
                                                "Target must be a creature.")),
                                new ChooseOneEffect.ChooseOneOption(
                                        "You gain 5 life",
                                        new GainLifeEffect(5))))),
                "{2}, Sacrifice two artifacts: Choose one — Breya deals 3 damage to target player or planeswalker; target creature gets -4/-4 until end of turn; or you gain 5 life."
        ).withModalChoiceAtActivation());
    }
}
