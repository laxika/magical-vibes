package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "63")
public class ThePit extends Card {

    public ThePit() {
        CreateTokenEffect angel = new CreateTokenEffect(
                "Angel", 3, 3, CardColor.WHITE, List.of(CardSubtype.ANGEL),
                Set.of(Keyword.FLYING), Set.of());

        Map<EffectSlot, CardEffect> demonEffects = Map.of(
                EffectSlot.UPKEEP_TRIGGERED, new SacrificeOtherCreatureOrDamageEffect(6));
        CreateTokenEffect demon = new CreateTokenEffect(
                1, "Demon", 6, 6, CardColor.BLACK, List.of(CardSubtype.DEMON),
                Set.of(Keyword.FLYING, Keyword.TRAMPLE), Set.of(), demonEffects);

        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, new EachPlayerChoosesTokenEffect(List.of(
                new EachPlayerChoosesTokenEffect.TokenOption("Create a 3/3 white Angel token", angel),
                new EachPlayerChoosesTokenEffect.TokenOption("Create a 6/6 black Demon token", demon))));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate()))),
                        SacrificeRecipient.EACH_PLAYER));
    }
}
