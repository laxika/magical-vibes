package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "197")
public class KaslemsStonetree extends Card {

    public KaslemsStonetree() {
        setBackFaceCard(new KaslemsStrider());

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                LookAtTopCardsEffect.mayPutOneMatchingOntoBattlefieldRestOnBottomRandom(
                        6, new CardTypePredicate(CardType.LAND)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}",
                List.of(new ExileSelfCost(), new CraftMaterialCost(CardSubtype.CAVE),
                        new ReturnSourceFromExileTransformedEffect()),
                "Craft with Cave {5}{G} ({5}{G}, Exile this artifact, Exile a Cave you control "
                        + "or a Cave card from your graveyard: Return this card transformed under its owner's "
                        + "control. Craft only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "KaslemsStrider";
    }
}
