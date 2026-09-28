package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DinosaurHeaddress;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "16")
@CardRegistration(set = "LCC", collectorNumber = "36")
public class PaleontologistsPickAxe extends Card {

    public PaleontologistsPickAxe() {
        setBackFaceCard(new DinosaurHeaddress());

        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsHostOfSourceAuraPredicate(),
                        SequenceEffect.of(
                                new DrawCardEffect(1),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER))));

        addActivatedAbility(new EquipActivatedAbility("{1}"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(
                        new ExileSelfCost(),
                        new CraftMaterialCost(1, CardType.CREATURE, null, List.of(), false, false, true),
                        new ReturnSourceFromExileTransformedEffect()),
                "Craft with one or more creatures {5} ({5}, Exile this artifact, Exile one or more creatures "
                        + "you control and/or creature cards from your graveyard: Return this card transformed "
                        + "under its owner's control. Craft only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "DinosaurHeaddress";
    }
}
