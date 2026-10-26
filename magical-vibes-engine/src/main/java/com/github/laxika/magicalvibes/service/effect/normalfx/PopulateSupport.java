package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The two halves of populate (CR 701.36a): finding the creature tokens a player controls, and
 * creating the copy of the one they chose.
 *
 * <p>{@link PopulateEffectHandler} needs the first half to decide between "no token, do nothing",
 * "one token, forced choice" and "prompt"; {@code PermanentChoiceBattlefieldHandlerService} needs
 * the second half once the prompt is answered.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PopulateSupport {

    private final GameQueryService gameQueryService;
    private final ObjectProvider<TokenCopySupport> tokenCopySupport;

    /** The creature tokens {@code controllerId} controls that can be chosen — the legal populate choices. */
    public List<Permanent> creatureTokensControlledBy(GameData gameData, UUID controllerId) {
        List<Permanent> tokens = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return tokens;
        }
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isToken(gameData, permanent) && gameQueryService.isCreature(gameData, permanent)
                    && !gameQueryService.cantBeChosen(gameData, permanent)) {
                tokens.add(permanent);
            }
        }
        return tokens;
    }

    /**
     * Creates a token copy of {@code sourceToken} for {@code controllerId} through the shared token-copy pipeline:
     * one token creation event whose count replacements and additions the player orders (CR 616.1), offered to
     * Jinnie Fay, and whose copies and added tokens enter together. A copy that "enters tapped and attacking"
     * (Ghired, Conclave Exile) enters tapped; the entry's following attack step makes it attacking.
     */
    public void createCopy(GameData gameData, UUID controllerId, Permanent sourceToken, StackEntry entry) {
        CreateTokenCopyOfTargetPermanentEffect copyEffect =
                MakeCreatedPermanentsAttackingEffectHandler.entersTappedAndAttacking(entry)
                        ? CreateTokenCopyOfTargetPermanentEffect.tappedTokenCopy()
                        : new CreateTokenCopyOfTargetPermanentEffect();
        log.info("Game {} - Populate creates a token copy of {}", gameData.id, sourceToken.getCard().getName());
        tokenCopySupport.getObject().createTokenCopies(gameData, entry, List.of(sourceToken.getCard()), null,
                controllerId, copyEffect);
    }
}
