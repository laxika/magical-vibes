package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "117")
public class BreyasApprentice extends Card {

    public BreyasApprentice() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Thopter", 1, 1, null,
                        List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING),
                        Set.of(CardType.ARTIFACT)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Exile the top card of your library. Until the end of your next turn, you may play that card.",
                                        new ExileTopCardsMayPlayUntilNextTurnEffect(1)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Target creature gets +2/+0 until end of turn.",
                                        new BoostTargetCreatureEffect(2, 0),
                                        TargetFilters.creature())))
                ),
                "{T}, Sacrifice an artifact: Choose one — Exile the top card of your library. Until the end of your next turn, you may play that card; or target creature gets +2/+0 until end of turn."
        ).withModalChoiceAtActivation());
    }
}
