package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfExiledCostCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "8")
public class OsgirTheReconstructor extends Card {

    public OsgirTheReconstructor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact"),
                        new BoostTargetCreatureEffect(2, 0)),
                "{1}, Sacrifice an artifact: Target creature you control gets +2/+0 until end of turn.",
                TargetFilters.creatureYouControl()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(
                        ExileCardFromGraveyardCost.withManaValueEqualsX(CardType.ARTIFACT, true),
                        new CreateTokenCopyOfExiledCostCardEffect(2)),
                "{X}, {T}, Exile an artifact card with mana value X from your graveyard: Create two tokens that are copies of the exiled card. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
