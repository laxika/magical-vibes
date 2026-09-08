package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.MeldWithNamedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "225")
public class UrzaLordProtector extends Card {

    private static final String PARTNER_NAME = "The Mightstone and Weakstone";

    public UrzaLordProtector() {
        setBackFaceCard(new UrzaPlaneswalker());

        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )), 1, CostModificationScope.SELF));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}",
                List.of(new ConditionalEffect(
                        new AllOf(List.of(
                                new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                        new PermanentIsSourceCardPredicate(),
                                        new PermanentOwnedBySourceControllerPredicate()))),
                                new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                        new PermanentNamedPredicate(PARTNER_NAME),
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentOwnedBySourceControllerPredicate())))
                        )),
                        new MeldWithNamedCreatureEffect(PARTNER_NAME, new PermanentIsArtifactPredicate()))),
                "{7}: If you both own and control Urza, Lord Protector and an artifact named The Mightstone "
                        + "and Weakstone, exile them, then meld them into Urza, Planeswalker. Activate only as a sorcery."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "UrzaPlaneswalker";
    }
}
