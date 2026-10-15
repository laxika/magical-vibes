package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "339")
public class KneelBeforeMyLegions extends Card {

    public KneelBeforeMyLegions() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 4/4 colorless Scarecrow artifact creature token with vigilance",
                        new CreateTokenEffect("Scarecrow", 4, 4, null,
                                List.of(CardSubtype.SCARECROW), Set.of(Keyword.VIGILANCE),
                                Set.of(CardType.ARTIFACT))),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +3/+3 and gain vigilance and trample until end of turn",
                        List.of(
                                new BoostAllOwnCreaturesEffect(3, 3),
                                new GrantKeywordEffect(Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE),
                                        GrantScope.OWN_CREATURES))))));
    }
}
